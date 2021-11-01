package com.example.ytsample.utils

import android.app.Application
import android.content.Context
import androidx.work.WorkManager

class ApplicationClass : Application() {


    override fun onCreate() {
        super.onCreate()
        //workManager = WorkManager.getInstance(this)
    }


    companion object {
        @JvmStatic
        var instances: Context? = null
        var workManager: WorkManager? = null
    }

    init {
        instances = getInstance()
    }

    private fun getInstance(): Context {
        return this.applicationContext
    }

}