package com.example.ytsample.ui.home

import android.content.ContentValues
import android.content.Context
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
import com.example.ytsample.entities.DownloadResult
import com.example.ytsample.utils.Constants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.io.ByteReadChannel
import kotlin.math.roundToInt
import java.io.*


class DownLoadFileWorkManager(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private var liveDataHelper: LiveDataHelper? = null
        private var MEGABYTE: Long = 1024L * 1024L;
        private var isDownloadCompleted = false
        private var progress = 0

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
        val ktor = HttpClient(Android){
            engine {
                threadsCount = 4
                pipelining = true
            }
        }

        val notifyId: Int = System.currentTimeMillis().toInt()
        setForeground(
            createForegroundInfo(
                downloadedData.downloadTitle,
                0,
                0,
                notifyId, 0
            )
        )
        ktor.downloadFile(applicationContext, downloadedData,ktor).collect {
            when (it) {
                is DownloadResult.Success -> {
                    setForeground(downloadFinished("Download completed", notifyId))
                }
                is DownloadResult.Error -> {
                    Toast.makeText(applicationContext,"failed to download ",Toast.LENGTH_LONG).show()
                }
                is DownloadResult.Progress -> {
                    setForeground(
                        createForegroundInfo(
                            downloadedData.downloadTitle,
                            it.progress,
                            100, notifyId, it.downloadedData
                        )
                    )
                    setProgress(workDataOf(Constants.PROGRESS to it.progress))
                }
            }
        }

        return Result.success()
    }


    private fun createForegroundInfo(
        downloadTitle: String?,
        progress: Int,
        max: Int, id: Int, data: Int
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
                ?.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                ?.setStyle(
                    NotificationCompat.InboxStyle()
                        .addLine("$progress%").addLine(data.toString())
                )
                ?.build()

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

    private fun bytesToMeg(bytes: Float): Float {
        return (bytes / MEGABYTE)
    }

    @Throws(IOException::class)
    private suspend fun HttpClient.downloadFile(
        ctx: Context,
        downloadedData: DownloadedData,
        ktor: HttpClient
    ): Flow<DownloadResult> {
        return flow {
            try {
                val response: HttpResponse = ktor.get(downloadedData.youtubeDlUrl!!){
                    headers{
                       // append(HttpHeaders.UserAgent,YouTubeUtils.USER_AGENT)
                    }
                }
                var output: OutputStream? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = ctx.applicationContext.contentResolver
                    val values = ContentValues()
                    values.put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        if (downloadedData.isVideo == true) "file_${System.currentTimeMillis()}.mp4" else "file_${System.currentTimeMillis()}.mp3"
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
                        if (downloadedData.isVideo == true) "file_${System.currentTimeMillis()}.mp4" else "file_${System.currentTimeMillis()}.mp3"
                    )
                    // Output stream to write file
                    output = FileOutputStream(file, true)
                }


                val channel = response.receive<ByteReadChannel>()
                val contentLen = response.contentLength()?.toInt() ?: 0
                val data = ByteArray(contentLen)
                val byteBufferSize = (1024 * 1024) * 16
                var offset = 0
                do {
                    val currentRead = channel.readAvailable(data, offset, data.size)
                    offset += currentRead
                    val mb = offset
                    val progress = (mb * 100f / data.size).roundToInt()
                    emit(DownloadResult.Progress(progress, mb))
                } while (currentRead > 0)

                response.close()

                if (response.status.isSuccess()) {
                    withContext(Dispatchers.IO) {
                        output?.write(data)
                    }
                    emit(DownloadResult.Success)
                } else {
                    emit(DownloadResult.Error("File not downloaded"))
                }
            } catch (e: TimeoutCancellationException) {
                emit(DownloadResult.Error("Connection timed out", e))
            } catch (t: Throwable) {
                emit(DownloadResult.Error("Failed to connect"))
            }
        }
    }
}