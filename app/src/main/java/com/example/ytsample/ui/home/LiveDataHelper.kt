package com.example.ytsample.ui.home

import androidx.lifecycle.LiveData

import androidx.lifecycle.MediatorLiveData
import com.example.ytsample.entities.Download
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.ProgressState


class LiveDataHelper private constructor() {
    private val state = MediatorLiveData<MutableMap<String, Download>?>()
    private val data = MediatorLiveData<ProgressState>()
    var downloadDatas = mutableMapOf<String,Download>()

    fun updatePercentage(percentage: MutableMap<String, Download>?) {
        state.postValue(percentage)
    }

    fun observePercentage(): MediatorLiveData<MutableMap<String, Download>?> {
        return state
    }

    fun addData(percentage: ProgressState) {
        data.postValue(percentage)
    }

    fun observeData(): LiveData<ProgressState> {
        return data
    }

    companion object {
        private var liveDataHelper: LiveDataHelper? = null

        @get:Synchronized
        val instance: LiveDataHelper?
            get() {
                if (liveDataHelper == null) liveDataHelper = LiveDataHelper()
                return liveDataHelper
            }
    }
}