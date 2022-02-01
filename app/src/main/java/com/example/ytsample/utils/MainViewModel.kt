package com.example.ytsample.utils

import android.app.Application
import android.util.SparseArray
import androidx.lifecycle.*
import androidx.work.*
import com.example.ytsample.database.YoDoDatabase
import com.example.ytsample.entities.*
import com.example.ytsample.entities.dbentities.DownloadedFile
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
    val yoDoRespository: YoDoRespository
    val yodoDB: YoDoDatabase
    val downloadFileLiveDataList = MutableLiveData<List<DownloadedFile>?>()
    val FORMAT_MAP: SparseArray<Format> = SparseArray<Format>()

    init {
        yodoDB = YoDoDatabase.getDatabase(application)
        yoDoRespository = YoDoRespository(yodoDB.downloadedFileDAO())
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
        progressWorkInfoItems = workManager.getWorkInfosByTagLiveData(TAG_PROGRESS)
        initFormat()
    }

    private fun initFormat() {
        // http://en.wikipedia.org/wiki/YouTube#Quality_and_formats

        // Video and Audio
        FORMAT_MAP.put(
            17,
            Format(17, "3gp", 144, Format.VCodec.MPEG4, Format.ACodec.AAC, 24, false)
        )
        FORMAT_MAP.put(
            36,
            Format(36, "3gp", 240, Format.VCodec.MPEG4, Format.ACodec.AAC, 32, false)
        )
        FORMAT_MAP.put(
            5,
            Format(5, "flv", 240, Format.VCodec.H263, Format.ACodec.MP3, 64, false)
        )
        FORMAT_MAP.put(
            43,
            Format(43, "webm", 360, Format.VCodec.VP8, Format.ACodec.VORBIS, 128, false)
        )
        FORMAT_MAP.put(
            18,
            Format(18, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 96, false)
        )
        FORMAT_MAP.put(
            22,
            Format(22, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 192, false)
        )

        // Dash Video

        // Dash Video
        FORMAT_MAP.put(
            160,
            Format(160, "mp4", 144, Format.VCodec.H264, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            133,
            Format(133, "mp4", 240, Format.VCodec.H264, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            134,
            Format(134, "mp4", 360, Format.VCodec.H264, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            135,
            Format(135, "mp4", 480, Format.VCodec.H264, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            136,
            Format(136, "mp4", 720, Format.VCodec.H264, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            137,
            Format(137, "mp4", 1080, Format.VCodec.H264, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            264,
            Format(264, "mp4", 1440, Format.VCodec.H264, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            266,
            Format(266, "mp4", 2160, Format.VCodec.H264, Format.ACodec.NONE, true)
        )

        FORMAT_MAP.put(
            298,
            Format(298, "mp4", 720, Format.VCodec.H264, 60, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            299,
            Format(299, "mp4", 1080, Format.VCodec.H264, 60, Format.ACodec.NONE, true)
        )

        // Dash Audio

        // Dash Audio
        FORMAT_MAP.put(
            140,
            Format(140, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 128, true)
        )
        FORMAT_MAP.put(
            141,
            Format(141, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 256, true)
        )
        FORMAT_MAP.put(
            256,
            Format(256, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 192, true)
        )
        FORMAT_MAP.put(
            258,
            Format(258, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 384, true)
        )

        // WEBM Dash Video

        // WEBM Dash Video
        FORMAT_MAP.put(
            278,
            Format(278, "webm", 144, Format.VCodec.VP9, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            242,
            Format(242, "webm", 240, Format.VCodec.VP9, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            243,
            Format(243, "webm", 360, Format.VCodec.VP9, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            244,
            Format(244, "webm", 480, Format.VCodec.VP9, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            247,
            Format(247, "webm", 720, Format.VCodec.VP9, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            248,
            Format(248, "webm", 1080, Format.VCodec.VP9, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            271,
            Format(271, "webm", 1440, Format.VCodec.VP9, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            313,
            Format(313, "webm", 2160, Format.VCodec.VP9, Format.ACodec.NONE, true)
        )

        FORMAT_MAP.put(
            302,
            Format(302, "webm", 720, Format.VCodec.VP9, 60, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            308,
            Format(308, "webm", 1440, Format.VCodec.VP9, 60, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            303,
            Format(303, "webm", 1080, Format.VCodec.VP9, 60, Format.ACodec.NONE, true)
        )
        FORMAT_MAP.put(
            315,
            Format(315, "webm", 2160, Format.VCodec.VP9, 60, Format.ACodec.NONE, true)
        )

        // WEBM Dash Audio

        // WEBM Dash Audio
        FORMAT_MAP.put(
            171,
            Format(171, "webm", Format.VCodec.NONE, Format.ACodec.VORBIS, 128, true)
        )

        FORMAT_MAP.put(
            249,
            Format(249, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 48, true)
        )
        FORMAT_MAP.put(
            250,
            Format(250, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 64, true)
        )
        FORMAT_MAP.put(
            251,
            Format(251, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 160, true)
        )

        // HLS Live Stream

        // HLS Live Stream
        FORMAT_MAP.put(
            91,
            Format(91, "mp4", 144, Format.VCodec.H264, Format.ACodec.AAC, 48, false, true)
        )
        FORMAT_MAP.put(
            92,
            Format(92, "mp4", 240, Format.VCodec.H264, Format.ACodec.AAC, 48, false, true)
        )
        FORMAT_MAP.put(
            93,
            Format(93, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 128, false, true)
        )
        FORMAT_MAP.put(
            94,
            Format(94, "mp4", 480, Format.VCodec.H264, Format.ACodec.AAC, 128, false, true)
        )
        FORMAT_MAP.put(
            95,
            Format(95, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 256, false, true)
        )
        FORMAT_MAP.put(
            96,
            Format(96, "mp4", 1080, Format.VCodec.H264, Format.ACodec.AAC, 256, false, true)
        )
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
    }

    // downloaded file
    fun getAllDownloadedFileData() {
        viewModelScope.launch {
            yoDoRespository?.getAllDownloadedFile?.collect {
                if (it.isNotEmpty())
                    downloadFileLiveDataList.value = it
            }
        }
    }

    fun insertDownloadFile(data: DownloadedFile) {
        viewModelScope.launch(Dispatchers.IO) {
            yoDoRespository?.insertDownloadFile(data)
        }
    }

    fun deleteDownloadFileById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            yoDoRespository?.deleteDownloadFileById(id)
        }
    }

    fun updateDownloadedFile(id: String,isDownloadSuccess: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            yoDoRespository?.updateDownloadFile(id,isDownloadSuccess)
        }
    }

}