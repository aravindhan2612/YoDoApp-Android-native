package com.example.ytsample.utils

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.ytsample.BuildConfig.DEBUG

class ApplicationClass : Application() ,Configuration.Provider{


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

    override fun getWorkManagerConfiguration(): Configuration {
        return if (DEBUG) {
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        } else {
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.ERROR)
                .build()
        }
    }

}