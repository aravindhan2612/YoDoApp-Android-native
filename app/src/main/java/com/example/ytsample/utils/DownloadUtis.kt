package com.example.ytsample.utils

import android.content.Context
import android.webkit.MimeTypeMap
import com.example.ytsample.entities.DownloadResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.cio.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.io.copyAndClose
import java.io.File
import kotlin.math.roundToInt

val globalContext: Context?
    get() = ApplicationClass.instances?.applicationContext


suspend fun HttpClient.downloadFile(file: File, url: String, callback: suspend (boolean: Boolean) -> Unit) {
    val call = call {
        url(url)
        method = HttpMethod.Get
    }
    if (!call.response.status.isSuccess()) {
        callback(false)
    }
    call.response.content.copyAndClose(file.writeChannel())
    callback(true)
}

suspend fun HttpClient.downloadFile(file: File, url: String): Flow<DownloadResult> {
    return flow {
        val response = call {
            url(url)
            method = HttpMethod.Get
        }.response
        val data = ByteArray(response.contentLength()!!.toInt())
        var offset = 0
        do {
            val currentRead = response.content.readAvailable(data, offset, data.size)
            offset += currentRead
            val mb = offset
            val progress = (mb * 100f / data.size).roundToInt()
            emit(DownloadResult.Progress(progress,mb))
        } while (currentRead > 0)
        response.close()
        if (response.status.isSuccess()) {
            file.writeBytes(data)
            emit(DownloadResult.Success)
        } else {
            emit(DownloadResult.Error("File not downloaded"))
        }
    }
}



//fun Activity.openFile(file: File) {
//    Intent(Intent.ACTION_VIEW).apply {
//        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//        addCategory(Intent.CATEGORY_DEFAULT)
//        val uri = FileProvider.getUriForFile(this@openFile, BuildConfig.APPLICATION_ID + ".provider", file)
//        val mimeType = getMimeType(file)
//        mimeType?.let {
//            setDataAndType(uri, it)
//            startActivity(this)
//        }
//
//    }
//}

fun getMimeType(file: File): String? {
    val extension = file.extension
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}