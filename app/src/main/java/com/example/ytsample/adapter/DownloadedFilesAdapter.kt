package com.example.ytsample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.ytsample.R
import com.example.ytsample.callbacks.IAdapterCallback
import com.example.ytsample.databinding.DownloadedFilesItemBinding
import com.example.ytsample.entities.dbentities.DownloadedFile
import com.example.ytsample.ui.downloads.DownloadsFragment
import com.example.ytsample.utils.MainViewModel

class DownloadedFilesAdapter(
    var downloadedFileList: ArrayList<DownloadedFile>?, var context: Context,
    downloadsFragment: DownloadsFragment, var adapterCallback: IAdapterCallback
) : RecyclerView.Adapter<DownloadedFilesAdapter.Holder>() {

    private lateinit var downloadedFilesItemBinding: DownloadedFilesItemBinding
    private var mainViewModel: MainViewModel? = null

    init {
        mainViewModel = ViewModelProvider(downloadsFragment).get(MainViewModel::class.java)
    }

    inner class Holder(itemView: DownloadedFilesItemBinding) :
        RecyclerView.ViewHolder(itemView.root),
        View.OnClickListener {
        val binding = itemView

        init {
            binding.deleteBtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.delete_btn -> {
                    val item = downloadedFileList?.get(adapterPosition)
                    adapterCallback.onItemSelected(item, true)
                    downloadedFileList?.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        downloadedFilesItemBinding =
            DownloadedFilesItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(downloadedFilesItemBinding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val ytDownloadData = downloadedFileList?.get(position)
        if (ytDownloadData != null) {
            holder.binding.cardView.visibility = View.VISIBLE
            holder.binding.downloadedTitleName.text = ytDownloadData.fileName
            if (ytDownloadData.isDownloadedSuccess) {
                holder.binding.downloadedStatus.text =
                    context.getString(R.string.downloaded_successfully)
                holder.binding.downloadedStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.success
                    )
                )
            } else {
                holder.binding.downloadedStatus.text = context.getString(R.string.download_failed)
                holder.binding.downloadedStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.failed
                    )
                )
            }
        } else {
            holder.binding.cardView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return downloadedFileList?.size ?: 0
    }

    fun refreshDBList(newDownloadFileList: ArrayList<DownloadedFile>?) {
        downloadedFileList = newDownloadFileList
        notifyDataSetChanged()
    }


}