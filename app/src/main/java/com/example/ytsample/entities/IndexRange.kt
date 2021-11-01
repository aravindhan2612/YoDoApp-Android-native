package com.example.ytsample.entities


import com.google.gson.annotations.SerializedName

data class IndexRange(
    @SerializedName("end")
    var end: String?,
    @SerializedName("start")
    var start: String?
)