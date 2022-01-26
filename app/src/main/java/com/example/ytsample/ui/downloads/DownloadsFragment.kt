package com.example.ytsample.ui.downloads

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
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
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.entities.YTDownloadData
import com.example.ytsample.utils.MainViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.io.File

class DownloadsFragment : Fragment(),IAdapterCallback {

    private lateinit var viewModel: DownloadsViewModel
    private lateinit var _binding: DownloadsFragmentBinding
    private lateinit var mainActivity: MainActivity
    val args: DownloadsFragmentArgs by navArgs()
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
        mainActivityViewModel.getAllDownloadData()
        initAdapter(null, null)
        initDownloadedAdapter()
        mainActivityViewModel.progressWorkInfoItems.observe(viewLifecycleOwner, progressObserver())
        mainActivityViewModel.ytDownloadLiveDataList.observe(
            viewLifecycleOwner,
            Observer { list ->
                    _binding.downloadedRecyclerView.visibility = if (list.isNullOrEmpty())View.GONE  else View.VISIBLE
                    adapter?.refreshDBList(list as ArrayList<YTDownloadData>?)
                    downloadedFilesAdapter?.refreshDBList(list as ArrayList<YTDownloadData>?)
                    val isDownload = list?.any { it.isFileDownload }
                    _binding.textDownload.visibility = if (isDownload == true) View.GONE else View.VISIBLE
            })
    }

    private fun initDownloadedAdapter() {
        if (downloadedFilesAdapter == null) {
            downloadedFilesAdapter = context?.let { DownloadedFilesAdapter(null, it, this,this) }
            downloadedFilesAdapter?.let {
                _binding.downloadedRecyclerView.adapter = it
            }
        }
    }

    private fun initAdapter(list: List<WorkInfo>?, dbList: ArrayList<YTDownloadData>?) {
        if (adapter == null) {
            adapter = context?.let { YTDownloadAdapter(list, dbList, it, this,this) }
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

    override fun onItemSelected(type: Any?,isDelete :Boolean) {
        type?.let {
            if (it is YTDownloadData) {
                mainActivityViewModel.workManager.cancelUniqueWork(it.id)
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        it.fileName
                    )
                    if (file?.exists()) {
                        file.delete()
                        Toast.makeText(context, "Deleted ${it.title}", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(
                            context,
                            "Error on deleting file from device ${it.title}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    if (isDelete) {
                        mainActivityViewModel.deleteData(it.id)
                    }

            }
        }

    }
}