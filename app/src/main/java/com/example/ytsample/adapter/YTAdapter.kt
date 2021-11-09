package com.example.ytsample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ytsample.R
import com.example.ytsample.databinding.YtItemBinding
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.FormatsModel
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.ui.bottomsheet.YtBottomSheetFragment

class YTAdapter(
    var list: ArrayList<FormatsModel>?,
    var meta: VideoMeta?,
    var context: Context,
    var homeFragment: YtBottomSheetFragment
) : RecyclerView.Adapter<YTAdapter.Holder>() {

    private lateinit var ytBinding: YtItemBinding

    inner class Holder(itemView: YtItemBinding) : RecyclerView.ViewHolder(itemView.root),
        View.OnClickListener {
        val binding = itemView

        init {
            binding.button.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val fm = list?.get(adapterPosition)
            val url = fm?.format?.url ?: fm?.adaptive?.url
            val isVideo = fm?.adaptive?.mimeType?.contains(
                "video",
                true
            ) == true || fm?.format?.mimeType?.contains("video", true) == true
            val bitrate = fm?.format?.bitrate ?: fm?.adaptive?.bitrate
            when (v?.id) {
                R.id.button -> {
                    homeFragment.downloadVideo(DownloadedData(url, meta?.title, isVideo, bitrate,System.currentTimeMillis().toInt()))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val fm = list?.get(position)
        fm?.let {
            if (it.format != null) {
                val videFormat = fm.format?.mimeType?.substringBefore(";")
                val tv =
                    if (it.format?.qualityLabel != null) fm.format?.qualityLabel + " $videFormat" else fm.format?.audioQuality
                holder.binding.textView.text = tv
            } else if (it.adaptive != null) {
                val videFormat = fm.adaptive?.mimeType?.substringBefore(";")
                val tv =
                    if (it.adaptive?.qualityLabel != null) fm.adaptive?.qualityLabel + " $videFormat" else fm.adaptive?.audioQuality
                holder.binding.textView.text = tv
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        ytBinding = YtItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(ytBinding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }
}