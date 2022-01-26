package com.example.ytsample.respository

import androidx.annotation.WorkerThread
import com.example.ytsample.dao.NotifyDAO
import com.example.ytsample.entities.YTDownloadData
import kotlinx.coroutines.flow.Flow

class YoDoRespository(private val notifyDAO: NotifyDAO) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allData: Flow<List<YTDownloadData>> = notifyDAO.getAll()
    fun getDownloadDataById(id:String): Flow<YTDownloadData> = notifyDAO.getDownloadDataById(id)

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(data: YTDownloadData) {
        notifyDAO.insert(data)
    }
    @WorkerThread
    suspend fun update(id: String,isFileDownloaded:Boolean,isDownloadSuccess:Boolean) {
        notifyDAO.updateById(id,isFileDownloaded,isDownloadSuccess)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteById(id:String) {
        notifyDAO.deleteById(id)
    }
}