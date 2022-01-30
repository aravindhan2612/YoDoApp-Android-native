package com.example.ytsample.wm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.utils.YTNotification
import com.google.gson.Gson
import androidx.work.*
import com.example.ytsample.R
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.network.RetrofitInterface
import kotlinx.coroutines.*
import java.io.*
import okhttp3.ResponseBody
import retrofit2.Retrofit
import com.example.ytsample.entities.Download
import com.example.ytsample.ui.home.LiveDataHelper
import com.example.ytsample.utils.Constants
import kotlinx.io.errors.IOException


class DownLoadFileWorkManager(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private var liveDataHelper: LiveDataHelper? = null
        private var MEGABYTE: Long = 1024L * 1024L;
        private var isDownloadCompleted = false
        var totalFileSize: Int = 0

        //private var progress = 0
        val GB: Long = 1000000000
        val MB: Long = 1000000
        val KB: Long = 1000
        val min: Long = 60
        val hours: Long = 3600
        var body: ResponseBody? = null

    }

    init {
        liveDataHelper = LiveDataHelper.instance
    }

    /**
     * Workmanager worker thread which do processing
     * in background, so it will not impact to main thread or UI
     *
     */

    override suspend fun doWork(): ListenableWorker.Result {
        val downloadedData: DownloadedData =
            Gson().fromJson(
                inputData.getString("downloadedData"),
                DownloadedData::class.java
            )
        withContext(Dispatchers.IO) {
            val notifyId: Int = System.currentTimeMillis().toInt()
            val retrofit = Retrofit.Builder()
                .baseUrl("http://localhost/")
                .build()
            val retrofitInterface = retrofit.create(RetrofitInterface::class.java)
            val request = downloadedData.youtubeDlUrl?.let {
                retrofitInterface.downloadFileUsingUrl(
                    it
                )
            }
            try {
                val output: OutputStream? = createFileOutPutStream(downloadedData)
                val download = Download()
                download.downloadTitle = downloadedData.downloadTitle
                download.id = id.toString()
                download.fileName = downloadedData.fileName
                LiveDataHelper.instance?.downloadDatas?.set(id.toString(), download)
                LiveDataHelper.instance?.updatePercentage( LiveDataHelper.instance?.downloadDatas)
                request?.execute()?.body()?.let { downloadFile(it, downloadedData, notifyId,download,output) }
            } catch (e: IOException) {

                e.printStackTrace();
                Toast.makeText(
                    applicationContext,
                    " Error on downloading  link",
                    Toast.LENGTH_SHORT
                ).show();

            }

        }
        downloadFinished(downloadedData.downloadTitle)
        return Result.success()
    }

    @Throws(IOException::class)
    private fun downloadFile(
        body: ResponseBody,
        downloadedData: DownloadedData,
        notifyId: Int,
        download: Download,
        output: OutputStream?
    ) {
        var count: Int
        val data = ByteArray(1024 * 4)
        val fileSize = body.contentLength()
        val bis: InputStream = BufferedInputStream(body.byteStream(), 1024 * 8)
        var total: Long = 0
        val startTime = System.currentTimeMillis()
        var timeCount = 1
        setProgressAsync(workDataOf(Constants.TITLE to downloadedData.downloadTitle))
        while (bis.read(data).also { count = it } != -1 && LiveDataHelper.instance?.downloadDatas?.get(this.id.toString()) != null) {
            total += count.toLong()
            totalFileSize = (fileSize / Math.pow(1024.0, 2.0)).toInt()
            val current = Math.round(total / Math.pow(1024.0, 2.0)).toDouble()
            val progress = (total * 100 / fileSize).toInt()
            val currentTime = System.currentTimeMillis() - startTime
            download.totalFileSize = totalFileSize
            if (currentTime > 1000 * timeCount) {
                download.currentFileSize = current.toInt()
                download.progress = progress
                setForegroundAsync(
                    createForegroundInfo(
                        downloadedData.downloadTitle,
                        100,
                        notifyId,
                        download
                    )
                )
                LiveDataHelper.instance?.downloadDatas?.set(this.id.toString(), download)
                LiveDataHelper.instance?.updatePercentage( LiveDataHelper.instance?.downloadDatas)
                setProgressAsync(workDataOf(Constants.PROGRESS to download.progress))
                timeCount++
            }
            output?.write(data, 0, count)
        }
        output?.flush()
        output?.close()
        bis.close()
    }

    private fun createFileOutPutStream(downloadedData: DownloadedData): OutputStream? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = applicationContext.contentResolver
            val values = ContentValues()
            values.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                downloadedData.fileName
            )
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS +"/YoDoApp"
            )
            var uri =
                resolver.insert(MediaStore.Files.getContentUri("external"), values)
            // Output stream to write file
           return uri?.let { resolver.openOutputStream(it) }
        } else {
             val file = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS +"/YoDoApp"
                ),
                 downloadedData.fileName
            )
            // Output stream to write file
            return FileOutputStream(file, true)
        }
    }

    private fun createForegroundInfo(
        downloadTitle: String?,
        max: Int, id: Int, data: Download?
    ): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        val builder = YTNotification(applicationContext).getNotificationBuilder()
        val pendingIntent = YTNotification(applicationContext).getPendingIntent()
        val notification =
            builder?.setSmallIcon(com.example.ytsample.R.drawable.ic_baseline_ondemand_video_24)
                ?.setContentTitle(downloadTitle)
                ?.setContentText("${data?.progress}%")
                ?.setContentIntent(pendingIntent)
                ?.setProgress(max, data?.progress ?: 0, false)
                ?.setOnlyAlertOnce(true)
                ?.setAutoCancel(false)
                ?.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                ?.setCategory(NotificationCompat.CATEGORY_EVENT)
                ?.setStyle(
                    NotificationCompat.InboxStyle()
                        .addLine("${data?.currentFileSize} /${data?.totalFileSize} MB")
                )
                ?.build()
        return ForegroundInfo(id, notification!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        YTNotification(applicationContext).createNotificationChannel()
    }


    private fun downloadFinished(downloadTitle: String?) {
        val id = System.currentTimeMillis().toInt()
        var notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.applicationContext.getString(R.string.channel_name)
            val descriptionText =
                applicationContext.applicationContext.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(YTNotification.CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            // Register the channel with the system

            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(
            applicationContext.applicationContext,
            YTNotification.CHANNEL_ID
        )
        val intent = Intent(applicationContext.applicationContext, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            this.putExtra("data", "fromoutside")
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext.applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification =
            builder.setSmallIcon(com.example.ytsample.R.drawable.ic_baseline_ondemand_video_24)
                .setContentTitle(downloadTitle)
                .setContentText("Download completed")
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)?.build()
        notificationManager?.notify(id, notification)
    }
}