package com.example.ytsample.dao

import androidx.room.*
import com.example.ytsample.entities.YTDownloadData
import kotlinx.coroutines.flow.Flow

@Dao
interface NotifyDAO {
    @Query("SELECT * FROM YTDownloadData")
    fun getAll(): Flow<List<YTDownloadData>>

    @Query("SELECT * FROM YTDownloadData WHERE id = :uid")
    fun getDownloadDataById(uid: String): Flow<YTDownloadData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(yTDownloadData: YTDownloadData)

    @Delete
    suspend fun delete(yTDownloadData: YTDownloadData)

    @Query("DELETE FROM YTDownloadData WHERE id = :uid")
    suspend fun deleteById(uid: String)

    @Query("UPDATE YTDownloadData SET isFileDownload =:isDownloaded, isDownloadedSuccess =:isDownloadedSuccess   WHERE id = :uid")
    suspend fun updateById(uid: String,isDownloaded:Boolean,isDownloadedSuccess: Boolean)
}