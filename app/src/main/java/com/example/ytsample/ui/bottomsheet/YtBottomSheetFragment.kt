package com.example.ytsample.ui.bottomsheet


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.R
import com.example.ytsample.databinding.YtBottomSheetFragmentBinding
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.FormatsModel
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.adapter.YTAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

import androidx.recyclerview.widget.DividerItemDecoration
import com.example.ytsample.utils.MainViewModel


class YtBottomSheetFragment() : BottomSheetDialogFragment(), View.OnClickListener {


    private lateinit var viewModel: YtBottomSheetViewModel
    private lateinit var mainActivityViewModel: MainViewModel
    private lateinit var binding: YtBottomSheetFragmentBinding
    val args: YtBottomSheetFragmentArgs by navArgs()
    private lateinit var mainActivity: MainActivity
    private var videoAdapter: YTAdapter? = null
    private var audioAdapter: YTAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = YtBottomSheetFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(false)
        viewModel = ViewModelProvider(this).get(YtBottomSheetViewModel::class.java)
        mainActivityViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.closeBtn.setOnClickListener(this)
        initObserver()
        initAllData()
    }

    private fun initObserver() {
        viewModel.responseJsonResult?.observe(viewLifecycleOwner, Observer { result ->
            if (result != null) {
                viewModel.extractUrl(result, requireContext())
            } else {
                binding.titleTv.text = getString(R.string.no_data)
            }
        })
        viewModel.text?.observe(viewLifecycleOwner, Observer { ytMetaData ->
            binding.progressCircular.visibility = View.GONE
            if (ytMetaData != null && !ytMetaData.list.isNullOrEmpty()) {
                binding.titleTv.visibility = View.VISIBLE
                binding.titleTv.text = ytMetaData.meta?.title
                val audioList =
                    ytMetaData.list.filter {
                        it.adaptive?.mimeType?.contains(
                            "audio",
                            true
                        ) == true || it.format?.mimeType?.contains("audio", true) == true
                    }
                val videoList =
                    ytMetaData.list.filter {
                        it.adaptive?.mimeType?.contains(
                            "video",
                            true
                        ) == true || it.format?.mimeType?.contains("video", true) == true
                    }
                initAudioAdapter(audioList as ArrayList<FormatsModel>, ytMetaData.meta)
                initVideoAdapter(videoList as ArrayList<FormatsModel>, ytMetaData.meta)
                viewModel.text = null
                viewModel.responseJsonResult = null
            }
        })
    }

    private fun initAllData() {
        binding.progressCircular.visibility = View.VISIBLE
        val urlLink = args.url
        if (!urlLink.isNullOrEmpty()) {
            viewModel.getRequest(
                requireContext(),
                args.url,
                this
            )
        } else {
            binding.titleTv.text = getString(R.string.no_data)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.close_btn -> {
                dismiss()
            }
        }
    }


    private fun initVideoAdapter(list: ArrayList<FormatsModel>, meta: VideoMeta?) {
        binding.videoSectionTv.visibility = View.VISIBLE
        binding.videoRecyclerView.visibility = View.VISIBLE
        binding.videoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        videoAdapter = YTAdapter(list, meta, requireContext(), this)
        binding.videoRecyclerView.adapter = videoAdapter
        binding.videoRecyclerView.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun initAudioAdapter(list: ArrayList<FormatsModel>, meta: VideoMeta?) {
        binding.audioRecyclerView.visibility = View.VISIBLE
        binding.audioSectionTv.visibility = View.VISIBLE
        binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        audioAdapter = YTAdapter(list, meta, requireContext(), this)
        binding.audioRecyclerView.adapter = audioAdapter
        binding.audioRecyclerView.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    fun downloadVideo(downloadedData: DownloadedData) {
        if (downloadedData.youtubeDlUrl != null) {
            mainActivityViewModel.downloadvideo(downloadedData)
            if (!args.isFromSend) {
                val action =
                    YtBottomSheetFragmentDirections.actionYtBottomSheetFragmentToNavigationDownload(
                        downloadedData
                    )
                findNavController().navigate(action)
            }
            dismiss()
        } else {
            view?.let {
                Snackbar.make(it, "Unable to download video ", Snackbar.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }

}