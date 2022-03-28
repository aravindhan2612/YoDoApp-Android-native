package com.example.ytsample.ui.downloads

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.work.WorkInfo
import com.example.ytsample.adapter.DownloadedFilesAdapter
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.adapter.YTDownloadAdapter
import com.example.ytsample.callbacks.IAdapterCallback
import com.example.ytsample.databinding.DownloadsFragmentBinding
import com.example.ytsample.entities.Download
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.dbentities.DownloadedFile
import com.example.ytsample.ui.home.LiveDataHelper
import com.example.ytsample.utils.MainViewModel
import kotlinx.io.errors.IOException
import java.io.File

class DownloadsFragment : Fragment(), IAdapterCallback {

    private lateinit var viewModel: DownloadsViewModel
    private lateinit var _binding: DownloadsFragmentBinding
    private lateinit var mainActivity: MainActivity
    private var data = ArrayList<DownloadedData>()
    private var adapter: YTDownloadAdapter? = null
    private lateinit var mainActivityViewModel: MainViewModel
    private var downloadedFilesAdapter: DownloadedFilesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DownloadsFragmentBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DownloadsViewModel::class.java)
        mainActivityViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainActivityViewModel.getAllDownloadedFileData()
        initAdapter(null, null)
        initDownloadedAdapter()
        mainActivityViewModel.progressWorkInfoItems.observe(viewLifecycleOwner, progressObserver())
        mainActivityViewModel.downloadFileLiveDataList.observe(viewLifecycleOwner) { list ->
            downloadedFilesAdapter?.refreshDBList(list as ArrayList<DownloadedFile>?)
            val isEmpty = (list.isNullOrEmpty())
            _binding.textDownload.visibility = if (isEmpty) View.VISIBLE else View.GONE
            _binding.downloadedRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
        LiveDataHelper.instance?.observePercentage()?.observe(viewLifecycleOwner) {
            adapter?.refreshDBList(it)
        }
    }

    private fun initDownloadedAdapter() {
        if (downloadedFilesAdapter == null) {
            downloadedFilesAdapter = context?.let { DownloadedFilesAdapter(null, it, this, this) }
            downloadedFilesAdapter?.let {
                _binding.downloadedRecyclerView.adapter = it
            }
        }
    }

    private fun initAdapter(list: ArrayList<WorkInfo>?, dbList: MutableMap<String, Download>?) {
        if (adapter == null) {
            adapter = context?.let { YTDownloadAdapter(list, dbList, it, this, this) }
            adapter?.let {
                _binding.recyclerView.adapter = it
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    private fun progressObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) {
                _binding.recyclerView.visibility = View.GONE
                return@Observer
            }
            listOfWorkInfo?.let {
                _binding.recyclerView.visibility = View.VISIBLE
                _binding.recyclerView.adapter = adapter
                adapter?.refreshList(it as ArrayList<WorkInfo>)
            }

        }
    }

    override fun onItemSelected(type: Any?, isDelete: Boolean) {
        type?.let {
            if (it is Download) {
                deleteFile(it.fileName)
            } else if (it is DownloadedFile) {
               deleteFile(it.fileName)
                if (isDelete) {
                    it.id?.let { id -> mainActivityViewModel.deleteDownloadFileById(id) }
                }
            }
        }
    }

    fun deleteFile(fileName: String?) {
            val file = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS + "/YoDoApp"
                ),
                fileName
            )
            if (file?.exists()) {
                file.delete()
                Toast.makeText(context, "Deleted $fileName", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(
                    context,
                    "Error on deleting file from device $fileName",
                    Toast.LENGTH_LONG
                ).show()
            }
    }


}