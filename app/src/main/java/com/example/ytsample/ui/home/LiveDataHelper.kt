package com.example.ytsample.ui.home

import androidx.lifecycle.LiveData

import androidx.lifecycle.MediatorLiveData
import com.example.ytsample.entities.ProgressState


class LiveDataHelper private constructor() {
    private val state = MediatorLiveData<ProgressState>()
    fun updatePercentage(percentage: ProgressState) {
        state.postValue(percentage)
    }

    fun observePercentage(): LiveData<ProgressState> {
        return state
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