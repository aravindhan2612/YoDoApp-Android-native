package com.example.ytsample.ui.downloads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.ytsample.databinding.YtDownloadItemBinding
import com.example.ytsample.entities.ProgressState
import com.example.ytsample.ui.home.LiveDataHelper
import com.google.android.material.snackbar.Snackbar

class YTDownloadAdapter(
    var list: ArrayList<ProgressState>?,
    var context: Context,
    val downloadsFragment: DownloadsFragment
) : RecyclerView.Adapter<YTDownloadAdapter.Holder>() {

    private lateinit var ytDownloadItemBinding: YtDownloadItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YTDownloadAdapter.Holder {
        ytDownloadItemBinding =
            YtDownloadItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(ytDownloadItemBinding)
    }

    override fun onBindViewHolder(holder: YTDownloadAdapter.Holder, position: Int) {
        var item = list?.get(position)
        LiveDataHelper.instance?.observePercentage()?.observe(downloadsFragment.viewLifecycleOwner, Observer {
            if (item?.id == it.id) {
                if (it != null && it.isFinished == false) {
                    if (it.percent in 0..100) {
                        holder.binding.titleTv.text = it.length.toString()
                        holder.binding.downloadProgressBar.progress = it.percent!!
                    }
                    if (it.percent == 100) {
                    }
                } else {
                }
            }
        })
        item?.let {
        }

    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    fun refreshList(  newList: ArrayList<ProgressState>?){
        list = newList
        notifyDataSetChanged()
    }

    inner class Holder(itemView: YtDownloadItemBinding) : RecyclerView.ViewHolder(itemView.root), View.OnClickListener {
        val binding = itemView

        init {
            binding.titleTv.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
        }
    }
}