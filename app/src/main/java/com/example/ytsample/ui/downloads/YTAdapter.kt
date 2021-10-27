package com.example.ytsample.ui.downloads

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ytsample.R
import com.example.ytsample.databinding.YtItemBinding
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.entities.YtFile

class YTAdapter(
    var list: ArrayList<YtFile>?,
    var meta: VideoMeta?,
    var context: Context,
    var downloadsFragment: DownloadsFragment
) : RecyclerView.Adapter<YTAdapter.Holder>() {

    private lateinit var ytBinding: YtItemBinding

    inner class Holder(itemView: YtItemBinding) : RecyclerView.ViewHolder(itemView.root),
        View.OnClickListener {
        val binding = itemView

        init {
            binding.button.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val url = list?.get(adapterPosition)?.url
            when (v?.id) {
                R.id.button -> downloadsFragment.downloadVideo(url, meta?.title, meta?.author)
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.binding.textView.text = list?.get(position)?.url.toString()
        holder.binding.textView2.text = meta?.title
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        ytBinding = YtItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(ytBinding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }
}