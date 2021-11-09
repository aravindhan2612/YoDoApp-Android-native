package com.example.ytsample.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.ytsample.R
import com.example.ytsample.entities.DownloadedData
import kotlinx.android.synthetic.main.yt_download_item.view.*

class YTDownloadAdapter2(var dummyes: ArrayList<DownloadedData>, val listener: (DownloadedData) -> Unit) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.yt_download_item, parent, false))

    override fun getItemCount(): Int = dummyes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dummy = dummyes[position]
        with(holder.itemView) {
            title_tv.text = dummy.downloadTitle
            download_progress_bar.isVisible = dummy.isDownloading
            download_progress_bar.isVisible = dummy.isDownloading
            setOnClickListener {
                listener(dummy)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.firstOrNull() != null) {
            with(holder.itemView) {
                (payloads.first() as Bundle).getInt("progress").also {
                    download_progress_bar.progress = it
                    percent.isVisible = it < 99
                    percent.text = "$it %"
                }
            }
        }
    }

    fun setDownloading(dummy: DownloadedData, isDownloading: Boolean) {
        getDummy(dummy)?.isDownloading = isDownloading
        notifyItemChanged(dummyes.indexOf(dummy))
    }

    fun setProgress(dummy: DownloadedData, progress: Int) {
        getDummy(dummy)?.progress = progress
        notifyItemChanged(dummyes.indexOf(dummy), Bundle().apply { putInt("progress", progress) })
    }

    private fun getDummy(dummy: DownloadedData) = dummyes.find { dummy.id == it.id }


}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view)