package com.example.ytsample.ui.bottomsheet


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.ytsample.MainActivity
import com.example.ytsample.R
import com.example.ytsample.databinding.YtBottomSheetFragmentBinding
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.FormatsModel
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.ui.downloads.YTAdapter
import com.example.ytsample.ui.home.DownLoadFileWorkManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import android.widget.Toast

import android.app.DownloadManager
import android.content.ContentValues
import android.content.IntentFilter
import android.net.Uri
import android.os.Build

import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream


class YtBottomSheetFragment() : BottomSheetDialogFragment(), View.OnClickListener {


    private lateinit var viewModel: YtBottomSheetViewModel
    private lateinit var binding: YtBottomSheetFragmentBinding
    val args: YtBottomSheetFragmentArgs by navArgs()
    private lateinit var mainActivity: MainActivity
    private var adapter: YTAdapter? = null


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
        binding.closeBtn.setOnClickListener(this)
        initObserver()
        initAllData()
    }

    private fun initObserver() {
        viewModel._responseResult?.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                viewModel.extractUrl(it, requireContext())
            }
        })
        viewModel.text?.observe(viewLifecycleOwner, Observer {
            binding.progressCircular.visibility = View.GONE
            if (it != null && !it.list.isNullOrEmpty()) {
                binding.titleTv.visibility = View.VISIBLE
                binding.titleTv.text = it.meta?.title
                initAdapter(it.list, it.meta)
                viewModel.text = null
                viewModel._responseResult = null
            }
        })
    }

    private fun initAllData() {
        if (!mainActivity.isWorkInfoRunning) {
            binding.progressCircular.visibility = View.VISIBLE
            viewModel.getRequest(
                requireContext(),
                args.url,
                this
            )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            com.example.ytsample.R.id.close_btn -> {
                dismiss()
            }
        }
    }


    private fun initAdapter(list: ArrayList<FormatsModel>, meta: VideoMeta?) {
        binding.recyclerView.visibility = View.VISIBLE
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = YTAdapter(list, meta, requireContext(), this)
        binding.recyclerView.adapter = adapter
    }

    fun downloadVideo(downloadedData: DownloadedData) {
        binding.recyclerView.visibility = View.GONE
        if (downloadedData.youtubeDlUrl != null) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val data = Data.Builder().putString("downloadedData", Gson().toJson(downloadedData)).build()
            val task = OneTimeWorkRequest.Builder(DownLoadFileWorkManager::class.java)
                .setInputData(data)
                .setConstraints(constraints).build()
            WorkManager.getInstance(requireContext().applicationContext).beginUniqueWork(task.id.toString(),ExistingWorkPolicy.APPEND_OR_REPLACE,task).enqueue()
            val action =
                YtBottomSheetFragmentDirections.actionYtBottomSheetFragmentToNavigationDownload(
                    downloadedData
                )
            findNavController().navigate(action)
            dismiss()
        } else {
            view?.let {
                Snackbar.make(it, "Unable to download video ", Snackbar.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }

}