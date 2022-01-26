package com.example.ytsample.ui.downloads

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ytsample.entities.DownloadResult
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.entities.YTMetaData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.roundToInt

class DownloadsViewModel : ViewModel() {
    private var _text = MutableLiveData<YTMetaData>()
    private var _meta = MutableLiveData<VideoMeta>()
    val text: LiveData<YTMetaData> = _text





}