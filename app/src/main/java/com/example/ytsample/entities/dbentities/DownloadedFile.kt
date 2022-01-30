package com.example.ytsample.entities.dbentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DownloadedFile")
class DownloadedFile( @PrimaryKey(autoGenerate = true) val uid: Int?,
                       @ColumnInfo(name = "id") var id: String?,
                       @ColumnInfo(name = "fileName") var fileName: String?,
                       @ColumnInfo(name = "isDownloadedSuccess") var isDownloadedSuccess: Boolean = false) {

}