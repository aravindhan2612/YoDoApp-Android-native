package com.example.ytsample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.ytsample.R
import com.example.ytsample.databinding.YtItemBinding
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.FormatsModel
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.ui.bottomsheet.YtBottomSheetFragment
import com.example.ytsample.utils.Constants
import com.example.ytsample.utils.MainViewModel

class YTAdapter(
    var list: ArrayList<FormatsModel>?,
    var meta: VideoMeta?,
    var context: Context,
    var homeFragment: YtBottomSheetFragment
) : RecyclerView.Adapter<YTAdapter.Holder>() {

    private lateinit var ytBinding: YtItemBinding
    private var mainViewModel: MainViewModel? = null

    init {
        mainViewModel = ViewModelProvider(homeFragment).get(MainViewModel::class.java)
    }

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
            val title = meta?.title?.substringBefore(":")
            var fileName ="${title}"+"${System.currentTimeMillis()}"
            fileName = fileName.filter { it.isLetterOrDigit() }
            val fileExtn = if (isVideo)".mp4" else ".mp3"
            fileName += fileExtn
            when (v?.id) {
                R.id.button -> {
                    homeFragment.downloadVideo(DownloadedData(url, meta?.title, fileName,System.currentTimeMillis().toInt(),isVideo))
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
                    if (it.format?.qualityLabel != null) fm.format?.qualityLabel + " $videFormat" else fm.format?.audioQuality + getBitrate(fm)
                holder.binding.textView.text = tv
            } else if (it.adaptive != null) {
                val videFormat = fm.adaptive?.mimeType?.substringBefore(";")
                val tv =
                    if (it.adaptive?.qualityLabel != null) fm.adaptive?.qualityLabel + " $videFormat" else fm.adaptive?.audioQuality + getBitrate(fm)
                holder.binding.textView.text = tv
            }

        }

    }

    private fun getBitrate(fm: FormatsModel): String {
        var bitrate:String = ""
        val formatVal = fm.itag?.let { it1 -> mainViewModel?.FORMAT_MAP?.get(it1) }
        if (formatVal != null){
            bitrate =" - " +formatVal.audioBitrate +" kbps"
        }
        return bitrate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        ytBinding = YtItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(ytBinding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }
}