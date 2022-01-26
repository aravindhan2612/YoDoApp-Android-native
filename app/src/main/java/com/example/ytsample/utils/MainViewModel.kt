package com.example.ytsample.utils

import android.app.Application
import androidx.lifecycle.*
import androidx.work.*
import com.example.ytsample.database.YoDoDatabase
import com.example.ytsample.entities.*
import com.example.ytsample.respository.YoDoRespository
import com.example.ytsample.wm.DownLoadFileWorkManager
import com.example.ytsample.utils.Constants.Companion.TAG_OUTPUT
import com.example.ytsample.utils.Constants.Companion.TAG_PROGRESS
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val workManager = WorkManager.getInstance(application)
    internal val outputWorkInfos: LiveData<List<WorkInfo>>
    internal val progressWorkInfoItems: LiveData<List<WorkInfo>>
    val ytDownloadLiveDataList = MutableLiveData<List<YTDownloadData>?>()
    val yoDoRespository: YoDoRespository
    val yodoDB: YoDoDatabase

    init {
        yodoDB = YoDoDatabase.getDatabase(application)
        yoDoRespository = YoDoRespository(yodoDB.notifyDAO())
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
            .setConstraints(constraints)
            .build()
        workManager.beginUniqueWork(task.id.toString(), ExistingWorkPolicy.APPEND_OR_REPLACE, task)
            .enqueue()
        val data = YTDownloadData(
            null,
            downloadedData.downloadTitle,
            task.id.toString(),
            false,
            downloadedData.fileName
        )
        insertData(data)
    }

    fun getAllDownloadData() {
        viewModelScope.launch {
            yoDoRespository.allData.collect {
                if (it.isNotEmpty())
                   ytDownloadLiveDataList.value = it
            }
        }
    }

    private fun insertData(data: YTDownloadData) {
        viewModelScope.launch(Dispatchers.IO) {
            yoDoRespository.insert(data)
        }
    }

    fun deleteData(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            yoDoRespository.deleteById(id)
        }
    }

    fun update(id: String, isDownload: Boolean,isDownloadSuccess: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            yoDoRespository.update(id, isDownload,isDownloadSuccess)
        }
    }

}