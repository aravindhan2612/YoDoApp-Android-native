package com.example.ytsample.entities

import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadedData(
    var youtubeDlUrl: String?,
    var downloadTitle: String?,
    var fileName: String?,
    var id:Int?,
    var isVideo: Boolean = false
) : Parcelable {
    var isDownloading: Boolean = false
    var progress = 0
}