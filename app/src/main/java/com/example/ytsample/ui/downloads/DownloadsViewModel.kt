package com.example.ytsample.ui.downloads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ytsample.entities.YTMetaData

class DownloadsViewModel : ViewModel() {
    private var _text = MutableLiveData<YTMetaData>()
    val text: LiveData<YTMetaData> = _text



}