package com.example.ytsample.adapter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.R
import com.example.ytsample.databinding.YtDownloadItemBinding
import com.example.ytsample.entities.YTDownloadData
import com.example.ytsample.ui.downloads.DownloadsFragment
import com.example.ytsample.utils.Constants
import com.example.ytsample.utils.MainViewModel
import com.example.ytsample.utils.YTNotification
import java.io.File

class YTDownloadAdapter(
    var list: List<WorkInfo>?,
    var context: Context,
    private val downloadsFragment: DownloadsFragment
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
        mainViewModel?.getDownloadDataById()
        val item = list?.get(position)
        println("***** item state " + item?.state)
        var currentYTDownloadData: YTDownloadData? = null
        mainViewModel?.ytDownloadLiveDataList?.observe(
            downloadsFragment,
            Observer { list ->
                if (list != null)
                    currentYTDownloadData =
                        list?.filter { it.id == item?.id.toString() }?.single()
            })
        when {
            item?.state == WorkInfo.State.ENQUEUED -> {
                holder.binding.downloadProgressBar.visibility = View.VISIBLE
                holder.binding.downloadProgressBar.isIndeterminate = true
            }
            item?.state == WorkInfo.State.RUNNING -> {
                val progress = item.progress.getInt(Constants.PROGRESS, 0)
                holder.binding.downloadProgressBar.isIndeterminate = false
                holder.binding.downloadProgressBar.setProgressCompat(progress, true)
                holder.binding.percent.text = "$progress%"
                holder.binding.titleTv.text = currentYTDownloadData?.title

            }
            item?.state == WorkInfo.State.SUCCEEDED -> {
                holder.binding.downloadProgressBar.visibility = View.GONE
                holder.binding.titleTv.visibility = View.GONE
                holder.binding.percent.visibility = View.GONE
                holder.binding.cardView.visibility = View.GONE
                if (currentYTDownloadData?.isFileDownload == false) {
                    mainViewModel?.workManager?.cancelWorkById(item.id)
                    mainViewModel?.update(item.id.toString(), true)
                   // downloadFinished(currentYTDownloadData?.title)
                }
            }
            else -> {
                holder.binding.downloadProgressBar.visibility = View.GONE
                holder.binding.titleTv.visibility = View.GONE
                holder.binding.percent.visibility = View.GONE
                holder.binding.cardView.visibility = View.GONE
            }
        }
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

    fun refreshList(newList: ArrayList<WorkInfo>) {
        list = newList
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
                    var element: YTDownloadData? = null
                    mainViewModel?.workManager?.cancelUniqueWork(item?.id.toString())
                    mainViewModel?.ytDownloadLiveDataList?.observe(
                        downloadsFragment,
                        Observer { list ->
                            element = list?.filter { it.id == item?.id.toString() }?.single()
                        })
                    element?.let {
                        val file = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            it.fileName
                        )
                        if (file?.exists()) {
                            file.delete()
                            mainViewModel?.deleteData(item?.id.toString())
                            Toast.makeText(context, "Deleted ${it.title}", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(
                                context,
                                "Error on deleting file from device ${it.title}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}