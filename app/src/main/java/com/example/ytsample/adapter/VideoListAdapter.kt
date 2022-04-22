package com.example.ytsample.adapter

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ytsample.R
import com.example.ytsample.callbacks.IAdapterCallback
import com.example.ytsample.databinding.VideoItemBinding
import com.example.ytsample.entities.video.Video


class VideoListAdapter(var videoList:ArrayList<Video>?,var context: Context,var adapterCallback :IAdapterCallback):RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {
    private lateinit var videoItemBinding: VideoItemBinding

    inner class ViewHolder(videoItemBinding: VideoItemBinding):RecyclerView.ViewHolder(videoItemBinding.root),
        View.OnClickListener {
        val binding = videoItemBinding
        init {
            binding.cardView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v?.id){
                R.id.card_view->{
                    adapterCallback.onItemSelected(videoList?.get(adapterPosition),false)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        videoItemBinding =
            VideoItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(videoItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        videoList?.get(position)?.let {
            val thumbnail: Bitmap? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.uri?.let { it1 ->
                        context.contentResolver.loadThumbnail(
                            it1, Size(400, 400), null)
                    }
                } else {
                    it.id?.let { id->
                        MediaStore.Video.Thumbnails.getThumbnail(
                            context.contentResolver,
                            id,
                            MediaStore.Video.Thumbnails.MICRO_KIND,
                            null as BitmapFactory.Options?
                        )
                    }
                }
            println("******* thumnail " + it.name + " "+ thumbnail?.height + " "+thumbnail?.width)
            holder.binding.videoThumbnail.setImageBitmap(thumbnail)
            holder.binding.videoTitleName.text = it.name
        }
    }

    override fun getItemCount(): Int {
        return videoList?.size ?:0
    }

    fun refreshList(newVideList: ArrayList<Video>) {
        videoList = newVideList
        notifyDataSetChanged()

    }
}