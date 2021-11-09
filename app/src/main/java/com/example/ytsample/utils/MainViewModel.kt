package com.example.ytsample.utils

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.*
import androidx.work.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.evgenii.jsevaluator.JsEvaluator
import com.evgenii.jsevaluator.interfaces.JsCallback
import com.example.ytsample.entities.*
import com.example.ytsample.ui.bottomsheet.YtBottomSheetFragmentDirections
import com.example.ytsample.ui.home.DownLoadFileWorkManager
import com.example.ytsample.utils.Constants.Companion.DOWNLOAD_VIDEO
import com.example.ytsample.utils.Constants.Companion.TAG_OUTPUT
import com.example.ytsample.utils.Constants.Companion.TAG_PROGRESS
import com.example.ytsample.utils.YouTubeUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val workManager = WorkManager.getInstance(application)
    internal val outputWorkInfos: LiveData<List<WorkInfo>>
    internal val progressWorkInfoItems: LiveData<List<WorkInfo>>

    init {

        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
        progressWorkInfoItems = workManager.getWorkInfosByTagLiveData(TAG_PROGRESS)
    }

    private fun createInputDataForUri(downloadedData: DownloadedData): Data {
        val builder = Data.Builder()
        builder.putString("downloadedData", Gson().toJson(downloadedData)).build()
        return builder.build()
    }

    internal fun downloadvideo(downloadedData: DownloadedData) {

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val task = OneTimeWorkRequest.Builder(DownLoadFileWorkManager::class.java)
            .setInputData(createInputDataForUri(downloadedData))
            .addTag(TAG_PROGRESS)
            .setConstraints(constraints).build()
        workManager.beginUniqueWork(task.id.toString(), ExistingWorkPolicy.APPEND_OR_REPLACE, task)
            .enqueue()

    }

}