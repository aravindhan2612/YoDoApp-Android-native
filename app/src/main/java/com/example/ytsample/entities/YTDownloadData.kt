package com.example.ytsample.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "YTDownloadData")
class YTDownloadData(
    @PrimaryKey(autoGenerate = true) val uid: Int?,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "isFileDownload") var isFileDownload: Boolean,
    @ColumnInfo(name = "fileName") var fileName: String?
) {

}
