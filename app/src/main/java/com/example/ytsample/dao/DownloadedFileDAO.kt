package com.example.ytsample.dao

import androidx.room.*
import com.example.ytsample.entities.dbentities.DownloadedFile
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedFileDAO {

    @Query("SELECT * FROM DownloadedFile")
    fun getDownloadedFileAll(): Flow<List<DownloadedFile>>

    @Query("SELECT * FROM DownloadedFile WHERE id = :uid")
    fun getDownloadedFilesById(uid: String): Flow<DownloadedFile>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDownloadedFile(downloadedFiles: DownloadedFile)

    @Delete
    suspend fun delete(downloadedFiles: DownloadedFile)

    @Query("DELETE FROM DownloadedFile WHERE id = :uid")
    suspend fun deleteDownloadedFileById(uid: String)

    @Query("UPDATE DownloadedFile SET  isDownloadedSuccess =:isDownloadedSuccess   WHERE id = :uid")
    suspend fun updateDownloadedFilesById(uid: String,isDownloadedSuccess: Boolean)
}