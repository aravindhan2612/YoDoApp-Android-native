package com.example.ytsample.ui.downloads

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ytsample.R
import com.example.ytsample.databinding.YtItemBinding
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.FormatsModel
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.entities.YtFile
import com.example.ytsample.ui.bottomsheet.YtBottomSheetFragment
import com.example.ytsample.ui.home.HomeFragment

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
            val url = fm?.format?.url?:fm?.adaptive?.url
            val qualityLabel = fm?.format?.qualityLabel?:fm?.adaptive?.qualityLabel
            when (v?.id) {
                R.id.button -> {
                    homeFragment.downloadVideo(DownloadedData( url, meta?.title,qualityLabel))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val fm = list?.get(position)
        fm?.let {
            if (it.format != null){
                val videFormat = fm.format?.mimeType?.substringBefore(";")
                var tv = if (it.format?.qualityLabel != null) fm.format?.qualityLabel+" $videFormat" else fm.format?.audioQuality
                holder.binding.textView.text = tv
            } else if (it.adaptive != null){
                val videFormat = fm.adaptive?.mimeType?.substringBefore(";")
                var tv = if (it.adaptive?.qualityLabel != null) fm.adaptive?.qualityLabel+" $videFormat" else fm.adaptive?.audioQuality
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