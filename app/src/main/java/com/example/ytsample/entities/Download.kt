package com.example.ytsample.entities

import android.os.Parcel

import android.os.Parcelable
import android.os.Parcelable.Creator
import kotlinx.android.parcel.Parcelize


@Parcelize
class Download() : Parcelable {

    var progress = 0
    var currentFileSize = 0
    var totalFileSize = 0
}