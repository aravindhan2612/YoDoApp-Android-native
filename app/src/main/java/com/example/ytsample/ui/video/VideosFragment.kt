package com.example.ytsample.ui.video

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ytsample.adapter.VideoListAdapter
import com.example.ytsample.callbacks.IAdapterCallback
import com.example.ytsample.databinding.FragmentVideosBinding
import com.example.ytsample.entities.video.Video
import com.google.android.material.snackbar.Snackbar

class VideosFragment:Fragment() ,IAdapterCallback{

    private lateinit var videosBinding: FragmentVideosBinding
    private var videoViewModel:VideoViewModel? = null
    private var videAdapter:VideoListAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        videosBinding = FragmentVideosBinding.inflate(inflater,container,false)
        return  videosBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoViewModel = ViewModelProvider(this).get(VideoViewModel::class.java)
        initVideoAdapter()
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchVideoList()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun initVideoAdapter() {
        videosBinding.videoListRecyclerView.layoutManager = GridLayoutManager(context,2)
        videAdapter = context?.let { VideoListAdapter(null, it,this) }
        videosBinding.videoListRecyclerView.adapter = videAdapter
    }

    private fun fetchVideoList() {
        videosBinding.videoFetchProgressBar.visibility = View.VISIBLE
        videoViewModel?.fetchVideo(context)
        videoViewModel?.videoListLiveData?.observe(viewLifecycleOwner) {
            it?.let { videoList->
                if (videoList.isNotEmpty()) {
                    videosBinding.videoFetchProgressBar.visibility = View.GONE
                    videosBinding.videoListRecyclerView.visibility = View.VISIBLE
                    videAdapter?.refreshList(it as ArrayList)
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                fetchVideoList()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                view?.let { Snackbar.make(it, "permission denied", Snackbar.LENGTH_SHORT) }
            }
        }

    override fun onItemSelected(type: Any?, isDelete: Boolean) {
        type?.let {
            if (it is Video){
                val action = VideosFragmentDirections.actionNavigationVideoToVideoPlayFragment(it)
                findNavController().navigate(action)
            }
        }
    }
}