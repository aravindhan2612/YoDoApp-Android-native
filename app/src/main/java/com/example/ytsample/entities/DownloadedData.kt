package com.example.ytsample.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadedData(
    var youtubeDlUrl: String?,
    var downloadTitle: String?,
    var fileName: String?,
    var id:Int?
) : Parcelable {
    var isDownloading: Boolean = false
    var progress = 0
}