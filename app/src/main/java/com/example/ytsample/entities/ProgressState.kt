package com.example.ytsample.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
 class ProgressState():Parcelable {
    var percent:Int? = null
    var length:Float? = null
    var onProgress:Float? = null
    var isFinished:Boolean? = null
    var title :String? = null
    var id:String? = null
}