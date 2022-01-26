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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.R
import com.example.ytsample.callbacks.IAdapterCallback
import com.example.ytsample.databinding.YtDownloadItemBinding
import com.example.ytsample.entities.YTDownloadData
import com.example.ytsample.ui.downloads.DownloadsFragment
import com.example.ytsample.utils.Constants
import com.example.ytsample.utils.MainViewModel
import com.example.ytsample.utils.YTNotification
import java.io.File

class YTDownloadAdapter(
    var list: List<WorkInfo>?,
    var dbList: ArrayList<YTDownloadData>?,
    var context: Context,
    private val downloadsFragment: DownloadsFragment,var adapterCallback: IAdapterCallback
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
        val ytDownloadData = dbList?.find { it.id == item?.id.toString() }
        if (ytDownloadData?.isFileDownload == false) {
            when (item?.state) {
                WorkInfo.State.RUNNING -> {
                    val progress = item.progress.getInt(Constants.PROGRESS, 0)
                    if (progress == 0) {
                        holder.binding.loadingMessageLayout.visibility = View.VISIBLE
                    } else {
                        holder.binding.loadingMessageLayout.visibility = View.GONE
                        holder.binding.downloadLayout.visibility = View.VISIBLE
                        holder.binding.titleTv.visibility = View.VISIBLE
                        holder.binding.downloadProgressBar.isIndeterminate = false
                        holder.binding.downloadProgressBar.setProgressCompat(progress, true)
                        holder.binding.percent.text = "$progress%"
                        holder.binding.titleTv.text = ytDownloadData.title
                    }
                }
                WorkInfo.State.SUCCEEDED -> {
                    holder.binding.cardView.visibility = View.GONE
                    if (!ytDownloadData.isFileDownload) {
                        mainViewModel?.workManager?.cancelWorkById(item.id)
                        mainViewModel?.update(item.id.toString(), true,true)
                        // downloadFinished(currentYTDownloadData?.title)
                    }
                }
                WorkInfo.State.CANCELLED -> {
                    holder.binding.cardView.visibility = View.GONE
                    if (!ytDownloadData.isFileDownload) {
                        mainViewModel?.update(item.id.toString(), true,false)
                    }

                }
                WorkInfo.State.FAILED ->{
                    holder.binding.cardView.visibility = View.GONE
                    if (!ytDownloadData.isFileDownload) {
                        mainViewModel?.update(item.id.toString(), true,false)
                    }
                }
                else -> {
                    holder.binding.cardView.visibility = View.GONE
                }
            }
        } else {
            holder.binding.cardView.visibility = View.GONE
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

    fun refreshList(newList: ArrayList<WorkInfo>?) {
        list = newList
        notifyDataSetChanged()
    }

    fun refreshDBList(newDbList: ArrayList<YTDownloadData>?) {
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
                    val element: YTDownloadData? = dbList?.find { it.id == item?.id.toString() }
                    adapterCallback.onItemSelected(element,false)
                }
            }
        }
    }
}