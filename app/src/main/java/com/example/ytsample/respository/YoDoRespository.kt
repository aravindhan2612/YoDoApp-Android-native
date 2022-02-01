package com.example.ytsample.respository

import androidx.annotation.WorkerThread
import com.example.ytsample.dao.DownloadedFileDAO
import com.example.ytsample.entities.dbentities.DownloadedFile
import kotlinx.coroutines.flow.Flow

class YoDoRespository(private val downloadedFileDAO: DownloadedFileDAO) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.

    @WorkerThread
    suspend fun insertDownloadFile(data: DownloadedFile) {
        downloadedFileDAO.insertDownloadedFile(data)
    }

    @WorkerThread
    suspend fun updateDownloadFile(id: String,isDownloadSuccess:Boolean) {
        downloadedFileDAO.updateDownloadedFilesById(id,isDownloadSuccess)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteDownloadFileById(id:String) {
        downloadedFileDAO.deleteDownloadedFileById(id)
    }

    val getAllDownloadedFile: Flow<List<DownloadedFile>> = downloadedFileDAO.getDownloadedFileAll()
}