package com.example.ytsample.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ytsample.dao.DownloadedFileDAO
import com.example.ytsample.entities.dbentities.DownloadedFile

@Database(entities = [DownloadedFile::class], version = 2)
public abstract class YoDoDatabase : RoomDatabase() {
    abstract fun downloadedFileDAO() : DownloadedFileDAO

    companion object {
        @Volatile
        private var INSTANCE: YoDoDatabase? = null

        fun getDatabase(context: Context):YoDoDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext,YoDoDatabase::class.java,"yoda_database").build()
                INSTANCE = instance
                instance
            }
        }
    }
}