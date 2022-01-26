package com.example.ytsample.entities

sealed class DownloadResult {
    object Success : DownloadResult()

    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()

    data class Progress(val progress: Int,val downloadedData: Int): DownloadResult()
}