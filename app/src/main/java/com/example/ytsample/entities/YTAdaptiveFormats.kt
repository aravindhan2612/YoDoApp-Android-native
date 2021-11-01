package com.example.ytsample.entities


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class YTAdaptiveFormats() : Parcelable {
    @SerializedName("approxDurationMs")
    var approxDurationMs: String? = null

    @SerializedName("averageBitrate")
    var averageBitrate: Int? = null

    @SerializedName("bitrate")
    var bitrate: Int? = null

    @SerializedName("contentLength")
    var contentLength: String? = null

    @SerializedName("fps")
    var fps: Int? = null

    @SerializedName("height")
    var height: Int? = null

    @SerializedName("highReplication")
    var highReplication: Boolean? = null

    @SerializedName("indexRange")
    var indexRange: IndexRange? = null

    @SerializedName("initRange")
    var initRange: InitRange? = null

    @SerializedName("itag")
    var itag: Int? = null

    @SerializedName("lastModified")
    var lastModified: String? = null

    @SerializedName("mimeType")
    var mimeType: String? = null

    @SerializedName("projectionType")
    var projectionType: String? = null

    @SerializedName("quality")
    var quality: String? = null

    @SerializedName("qualityLabel")
    var qualityLabel: String? = null

    @SerializedName("signatureCipher")
    var signatureCipher: String? = null

    @SerializedName("url")
    var url: String? = null

    @SerializedName("width")
    var width: Int = 0

    @SerializedName("audioChannels")
    var audioChannels: Int? = null

    @SerializedName("audioQuality")
    var audioQuality: String? = null

    @SerializedName("audioSampleRate")
    var audioSampleRate: String? = null
}