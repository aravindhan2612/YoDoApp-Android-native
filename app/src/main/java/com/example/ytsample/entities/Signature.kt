package com.example.ytsample.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Signature( val itag:Int?, val signature:String):Parcelable{

}
