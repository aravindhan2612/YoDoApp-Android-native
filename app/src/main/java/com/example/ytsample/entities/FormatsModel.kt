package com.example.ytsample.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FormatsModel(
    var format: YTFormat?,
    var adaptive: YTAdaptiveFormats?,
    val itag:Int?
):Parcelable {

}