package com.example.ytsample.ui.home

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.ProgressState
import com.example.ytsample.utils.YTNotification
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL
import androidx.annotation.RequiresApi
import androidx.work.*
import kotlinx.coroutines.delay
import java.net.HttpURLConnection


class DownLoadFileWorkManager(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        const val Progress = "Progress"
        private var liveDataHelper: LiveDataHelper? = null
        private var MEGABYTE: Long = 1024L * 1024L;
        var CHANNEL_ID: String = "YTSample"

        var TAG: Int = 1001;
        private var isNotified = false
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
        try {
            val downloadedData: DownloadedData =
                Gson().fromJson(
                    inputData.getString("downloadedData"),
                    DownloadedData::class.java
                )
            val url = URL(downloadedData.youtubeDlUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            // input stream to read file - with 8k buffer
            val input = BufferedInputStream(url.openStream(), 8192)
            var output: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = applicationContext.contentResolver
                val values = ContentValues()
                values.put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    "file_${System.currentTimeMillis()}.mp4"
                )

                values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )
                val uri =
                    resolver.insert(MediaStore.Files.getContentUri("external"), values)

                // Output stream to write file
                output = uri?.let { resolver.openOutputStream(it) }
            } else {
                var file = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ),
                    if (downloadedData.fileName != null) "file_${System.currentTimeMillis()}.mp4" else "file_${System.currentTimeMillis()}.mp3"
                )
                // Output stream to write file
                output = FileOutputStream(file, true)


            }

            val data = ByteArray(1024)

            var count: Int? = 0
            var bytesDownloaded: Int = 0

            val total: Long = bytesToMeg(connection.contentLength.toLong())
            val notifyId: Int = System.currentTimeMillis().toInt()
            setForeground(createForegroundInfo(downloadedData.downloadTitle, 0, 0, notifyId))
            LiveDataHelper?.instance?.addData(
                ProgressState(
                    0,
                    total,
                    0,
                    false,
                    notifyId.toString()
                )
            )
            while (run {
                    count = input.read(data)
                    count
                } != -1) {
                count?.let { bytesDownloaded += it }
                output?.write(data, 0, count!!)
                val percent = (bytesDownloaded * 100 / connection.contentLength)

                liveDataHelper?.updatePercentage(
                    ProgressState(
                        percent,
                        total, bytesToMeg(bytesDownloaded.toLong()), false, notifyId.toString()
                    )
                )
                setForegroundAsync(
                    createForegroundInfo(
                        downloadedData.downloadTitle,
                        percent,
                        100, notifyId
                    )
                )
                setProgress(workDataOf(Progress to percent))
            }
            delay(2000)
            setForeground(downloadFinished("Download completed", notifyId))
            // flushing output
            output?.flush()
            // closing streams
            output?.close()
            input.close()
            liveDataHelper?.updatePercentage(ProgressState(null, null, null, true,notifyId.toString()))

        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }

    private fun createForegroundInfo(
        downloadTitle: String?,
        progress: Int,
        max: Int, id: Int
    ): ForegroundInfo {

        val context = applicationContext
        // This PendingIntent can be used to cancel the worker
        // This PendingIntent can be used to cancel the worker

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        val builder = YTNotification(applicationContext).getNotificationBuilder()
        val pendingIntent = YTNotification(applicationContext).getPendingIntent()
        val notification =
            builder?.setSmallIcon(com.example.ytsample.R.drawable.ic_round_arrow_downward_24)
                ?.setContentTitle(downloadTitle)
                ?.setContentIntent(pendingIntent)
                ?.setProgress(max, progress, false)
                ?.setOnlyAlertOnce(true)
                ?.setAutoCancel(false)
                ?.setPriority(NotificationCompat.PRIORITY_DEFAULT)?.build()

        return ForegroundInfo(id, notification!!)

    }

    private fun downloadFinished(downloadTitle: String, id: Int): ForegroundInfo {

        // This PendingIntent can be used to cancel the worker
        // This PendingIntent can be used to cancel the worker

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        val builder = YTNotification(applicationContext).getNotificationBuilder()
        val pendingIntent = YTNotification(applicationContext).getPendingIntent()
        val notification =
            builder?.setSmallIcon(com.example.ytsample.R.drawable.ic_round_arrow_downward_24)
                ?.setContentTitle(downloadTitle)
                ?.setContentIntent(pendingIntent)
                ?.setProgress(0, 0, false)
                ?.setOnlyAlertOnce(true)
                ?.setAutoCancel(false)
                ?.setPriority(NotificationCompat.PRIORITY_DEFAULT)?.build()

        return ForegroundInfo(id, notification!!)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        YTNotification(applicationContext).createNotificationChannel()
    }

    private fun bytesToMeg(bytes: Long): Long {
        return bytes / MEGABYTE
    }
}

//function formatSizeUnits(bytes){
//    if      (bytes >= 1073741824) { bytes = (bytes / 1073741824).toFixed(2) + " GB"; }
//    else if (bytes >= 1048576)    { bytes = (bytes / 1048576).toFixed(2) + " MB"; }
//    else if (bytes >= 1024)       { bytes = (bytes / 1024).toFixed(2) + " KB"; }
//    else if (bytes > 1)           { bytes = bytes + " bytes"; }
//    else if (bytes == 1)          { bytes = bytes + " byte"; }
//    else                          { bytes = "0 bytes"; }
//    return bytes;
//}