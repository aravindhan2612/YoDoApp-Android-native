package com.example.ytsample.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.ytsample.MainActivity
import com.example.ytsample.R
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.ProgressState
import com.example.ytsample.utils.YTNotification
import com.google.gson.Gson
import kotlinx.coroutines.delay
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL


class DownLoadFileWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private var liveDataHelper: LiveDataHelper? = null
        private var MEGABYTE: Long = 1024L * 1024L;

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
    override fun doWork(): ListenableWorker.Result {
        try {
            val downloadedData: DownloadedData =
                Gson().fromJson(inputData.getString("downloadedData"), DownloadedData::class.java)
            val url = URL(downloadedData.youtubeDlUrl)
            val connection = url.openConnection()
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
            YTNotification(applicationContext).createNotificationChannel()
            val builder = YTNotification(applicationContext).getNotificationBuilder()
            val pendingIntent = YTNotification(applicationContext).getPendingIntent()
            builder?.setSmallIcon(R.drawable.ic_round_arrow_downward_24)
                ?.setContentTitle(downloadedData.downloadTitle)
                ?.setContentIntent(pendingIntent)
                ?.setOnlyAlertOnce(true)?.priority = NotificationCompat.PRIORITY_DEFAULT
            val notificationManager = YTNotification(applicationContext).getNotificationManager()
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
                        total, bytesToMeg(bytesDownloaded.toLong()), false
                    )
                )
                builder?.setProgress(100, percent, false)
                    ?.setContentText("$percent%")
                notificationManager?.notify(TAG, builder?.build())
            }
            // flushing output
            output?.flush()
            // closing streams
            output?.close()
            input.close()
            liveDataHelper?.updatePercentage(ProgressState(null, null, null, true))
            while (!isNotified) {
                Thread.sleep(1000)
                 builder?.setProgress(0, 0, false)?.setContentText("Download completed")?.setOngoing(false)
                YTNotification(applicationContext).getNotificationManager()
                    ?.notify(1001, builder?.build())
                isNotified = true
            }

        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
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