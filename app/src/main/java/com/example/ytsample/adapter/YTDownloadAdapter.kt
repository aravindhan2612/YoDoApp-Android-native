package com.example.ytsample.adapter

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.example.ytsample.R
import com.example.ytsample.databinding.YtDownloadItemBinding
import com.example.ytsample.entities.YTDownloadData
import com.example.ytsample.ui.downloads.DownloadsFragment
import com.example.ytsample.utils.Constants
import com.example.ytsample.utils.MainViewModel
import kotlinx.coroutines.launch
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
        if (item != null && (item.state != WorkInfo.State.CANCELLED)) {
            if (item.state == WorkInfo.State.RUNNING) {
                val progress = item.progress.getInt(Constants.PROGRESS, 0)
                holder.binding.downloadProgressBar.setProgressCompat(progress, true)
                holder.binding.percent.text = "$progress%"
                mainViewModel?.ytDownloadLiveDataList?.observe(
                    downloadsFragment,
                    Observer { list ->
                        if (list != null)
                            holder.binding.titleTv.text =
                                list?.filter { it.id == item?.id.toString() }?.single()?.title
                    })

            }
            if (item.state.isFinished) {
                holder.binding.downloadProgressBar.visibility = View.GONE
                holder.binding.titleTv.visibility = View.GONE
                holder.binding.percent.visibility = View.GONE
                holder.binding.cardView.visibility = View.GONE
                mainViewModel?.workManager?.cancelUniqueWork(item.id.toString())
                mainViewModel?.update(item.id.toString(), true)
            }
        } else {
            holder.binding.downloadProgressBar.visibility = View.GONE
            holder.binding.titleTv.visibility = View.GONE
            holder.binding.percent.visibility = View.GONE
            holder.binding.cardView.visibility = View.GONE
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
                        println("***** external file " + file)
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