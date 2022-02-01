package com.example.ytsample.entities


import android.os.Parcelable
import androidx.work.WorkInfo
import kotlinx.parcelize.Parcelize


@Parcelize
class Download(
    var progress: Int = 0,
    var currentFileSize: Int = 0,
    var totalFileSize: Int = 0,
    var downloadTitle: String? = null,
    var fileName: String? = null,
    var id: String? = null,
    var state: WorkInfo.State? = null,
    var isDownloadOrCancelled : Boolean = false
) : Parcelable {


}