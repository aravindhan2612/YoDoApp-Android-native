package com.example.ytsample.ui.video

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ytsample.R
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.databinding.FragmentVideoPlayerBinding

class VideoPlayFragment:Fragment() {

    private lateinit var videoPlayerBinding: FragmentVideoPlayerBinding
    val args: VideoPlayFragmentArgs by navArgs()
    private var mainActivity:MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        videoPlayerBinding = FragmentVideoPlayerBinding.inflate(inflater,container,false)
        return videoPlayerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        val mediaController = MediaController(context)
        mediaController.setAnchorView(videoPlayerBinding.videPlayerView)
        videoPlayerBinding.videPlayerView.setMediaController(mediaController)
        videoPlayerBinding.videPlayerView.setVideoURI(args.videoData?.uri)
        videoPlayerBinding.videPlayerView.requestFocus()
        videoPlayerBinding.videPlayerView.start()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity?
    }
}