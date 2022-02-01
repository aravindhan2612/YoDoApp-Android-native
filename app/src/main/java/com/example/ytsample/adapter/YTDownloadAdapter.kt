package com.example.ytsample.adapter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.R
import com.example.ytsample.callbacks.IAdapterCallback
import com.example.ytsample.databinding.YtDownloadItemBinding
import com.example.ytsample.entities.Download
import com.example.ytsample.entities.dbentities.DownloadedFile
import com.example.ytsample.ui.downloads.DownloadsFragment
import com.example.ytsample.ui.downloads.DownloadsViewModel
import com.example.ytsample.ui.home.LiveDataHelper
import com.example.ytsample.utils.Constants
import com.example.ytsample.utils.MainViewModel
import com.example.ytsample.utils.YTNotification

class YTDownloadAdapter(
    var list: ArrayList<WorkInfo>?,
    var dbList: MutableMap<String, Download>?,
    var context: Context,
    private val downloadsFragment: DownloadsFragment, var adapterCallback: IAdapterCallback
) : RecyclerView.Adapter<YTDownloadAdapter.Holder>() {

    private lateinit var ytDownloadItemBinding: YtDownloadItemBinding
    private var mainViewModel: MainViewModel? = null

    init {
        mainViewModel = ViewModelProvider(downloadsFragment).get(MainViewModel::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        ytDownloadItemBinding =
            YtDownloadItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(ytDownloadItemBinding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = list?.get(position)
        val download = dbList?.get(item?.id.toString())
        if (item?.state == WorkInfo.State.RUNNING) {
                val progress = item.progress.getInt(Constants.PROGRESS, 0)
                if (progress == 0) {
                    holder.binding.loadingMessageLayout.visibility = View.VISIBLE
                } else {
                    holder.binding.loadingMessageLayout.visibility = View.GONE
                    holder.binding.downloadLayout.visibility = View.VISIBLE
                    holder.binding.titleTv.visibility = View.VISIBLE
                    holder.binding.downloadProgressBar.isIndeterminate = false
                    holder.binding.downloadProgressBar.setProgressCompat(progress, true)
                    holder.binding.percent.text = download?.progress.toString() + "%"
                    holder.binding.titleTv.text = download?.downloadTitle
                    holder.binding.fileSize.text = "${download?.currentFileSize} /${download?.totalFileSize} MB"
                }
            }
        else {
            if (LiveDataHelper.instance?.downloadDatas?.get(item?.id.toString()) != null && download != null) {
                when(item?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        holder.binding.cardView.visibility = View.GONE
                        mainViewModel?.workManager?.cancelWorkById(item.id)
                        if (LiveDataHelper.instance?.downloadDatas?.get(item.id.toString()) != null) {
                            val downloadFile: DownloadedFile = buildDownloadFile(download, true)
                            mainViewModel?.insertDownloadFile(downloadFile)
                            LiveDataHelper.instance?.downloadDatas?.remove(download?.id.toString())
                            LiveDataHelper.instance?.updatePercentage(LiveDataHelper.instance?.downloadDatas)
                        }
                    }
                    WorkInfo.State.CANCELLED -> {
                        holder.binding.cardView.visibility = View.GONE
                        mainViewModel?.workManager?.cancelWorkById(item.id)
                        if (LiveDataHelper.instance?.downloadDatas?.get(item.id.toString()) != null) {
                            val downloadFile: DownloadedFile = buildDownloadFile(download, false)
                            mainViewModel?.insertDownloadFile(downloadFile)
                            LiveDataHelper.instance?.downloadDatas?.remove(download?.id.toString())
                            LiveDataHelper.instance?.updatePercentage(LiveDataHelper.instance?.downloadDatas)
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        holder.binding.cardView.visibility = View.GONE
                        if (LiveDataHelper.instance?.downloadDatas?.get(item.id.toString()) != null) {
                            val downloadFile: DownloadedFile = buildDownloadFile(download, false)
                            mainViewModel?.insertDownloadFile(downloadFile)
                            LiveDataHelper.instance?.downloadDatas?.remove(download?.id.toString())
                            LiveDataHelper.instance?.updatePercentage(LiveDataHelper.instance?.downloadDatas)
                        }
                    }
                    else -> {
                        holder.binding.cardView.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun buildDownloadFile(
        downloadingData: Download?,
        isDownloadSuccess: Boolean
    ): DownloadedFile {
        return DownloadedFile(
            null,
            downloadingData?.id,
            downloadingData?.fileName,
            isDownloadSuccess
        )
    }

    private fun downloadFinished(downloadTitle: String?) {
        val id = System.currentTimeMillis().toInt()
        var notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.applicationContext.getString(R.string.channel_name)
            val descriptionText = context.applicationContext.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(YTNotification.CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            // Register the channel with the system

            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(
            context.applicationContext,
            YTNotification.CHANNEL_ID
        )
        val intent = Intent(context.applicationContext, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            this.putExtra("data", "fromoutside")
        }
        val pendingIntent = PendingIntent.getActivity(
            context.applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification =
            builder.setSmallIcon(com.example.ytsample.R.drawable.ic_round_arrow_downward_24)
                .setContentTitle(downloadTitle)
                .setContentText("Download completed")
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)?.build()
        notificationManager?.notify(id, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        YTNotification(context).createNotificationChannel()
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    fun refreshList(newList: ArrayList<WorkInfo>?) {
        list = newList
        notifyDataSetChanged()
    }

    fun refreshDBList(newDbList: MutableMap<String, Download>?) {
        dbList = newDbList
        notifyDataSetChanged()
    }

    inner class Holder(itemView: YtDownloadItemBinding) : RecyclerView.ViewHolder(itemView.root),
        View.OnClickListener {
        val binding = itemView

        init {
            binding.titleTv.setOnClickListener(this)
            binding.cancelButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.cancel_button -> {
                    val item = list?.get(adapterPosition)
                    val element: Download? = dbList?.get(item?.id?.toString())
                    val downloadFile: DownloadedFile = buildDownloadFile(element, false)
                    mainViewModel?.insertDownloadFile(downloadFile)
                    adapterCallback.onItemSelected(element, false)
                    LiveDataHelper.instance?.downloadDatas?.remove(element?.id?.toString())
                    LiveDataHelper.instance?.updatePercentage( LiveDataHelper.instance?.downloadDatas)
                    item?.id?.let { mainViewModel?.workManager?.cancelWorkById(it) }
                }
            }
        }
    }
}