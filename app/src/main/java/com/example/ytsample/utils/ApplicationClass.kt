package com.example.ytsample.utils

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.ytsample.BuildConfig.DEBUG
import com.example.ytsample.database.YoDoDatabase
import com.example.ytsample.respository.YoDoRespository

class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()
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