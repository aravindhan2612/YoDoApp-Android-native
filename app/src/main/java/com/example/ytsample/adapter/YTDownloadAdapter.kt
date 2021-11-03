package com.example.ytsample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.ytsample.databinding.YtDownloadItemBinding
import com.example.ytsample.entities.ProgressState
import com.example.ytsample.ui.downloads.DownloadsFragment
import com.example.ytsample.ui.home.LiveDataHelper
import com.example.ytsample.utils.Constants

class YTDownloadAdapter(
    var list: List<WorkInfo>?,
    var context: Context,
    val downloadsFragment: DownloadsFragment
) : RecyclerView.Adapter<YTDownloadAdapter.Holder>() {

    private lateinit var ytDownloadItemBinding: YtDownloadItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        ytDownloadItemBinding =
            YtDownloadItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(ytDownloadItemBinding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = list?.get(position)
        if (item != null) {
            println("***** item state " + item.state + "  id " + item.id)
            if (item.state == WorkInfo.State.RUNNING) {
                val title = item.progress.getString(Constants.TITLE)
                val progress = item.progress.getInt(Constants.PROGRESS, 0)
                holder.binding.downloadProgressBar.progress = progress
                holder.binding.percent.text = "$progress%"
                holder.binding.titleTv.text = title
            }
            if (item.state.isFinished) {
                holder.binding.downloadProgressBar.visibility = View.GONE
                holder.binding.titleTv.visibility = View.GONE
                holder.binding.percent.visibility = View.GONE
                WorkManager.getInstance(context.applicationContext).cancelWorkById(item.id)
            }
        }
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
        }

        override fun onClick(v: View?) {
        }
    }
}