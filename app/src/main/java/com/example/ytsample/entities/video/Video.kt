package com.example.ytsample.entities.video

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val uri: Uri?,
    val name: String?,
    val duration: Int?,
    val size: Int?,
    val id: Long?
):Parcelable{

}
