package com.example.ytsample.ui.downloads

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.work.WorkInfo
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.adapter.YTDownloadAdapter
import com.example.ytsample.databinding.DownloadsFragmentBinding
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.utils.MainViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.*

class DownloadsFragment : Fragment() {

    private lateinit var viewModel: DownloadsViewModel
    private lateinit var _binding: DownloadsFragmentBinding
    private lateinit var mainActivity: MainActivity
    val args: DownloadsFragmentArgs by navArgs()
    private var data = ArrayList<DownloadedData>()
    private var adapter: YTDownloadAdapter? = null
    private lateinit var mainActivityViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DownloadsFragmentBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //if (data.any { it.id != args.downloadData?.id }){
          //  data.add(args.downloadData!!)
        //}
        viewModel = ViewModelProvider(this).get(DownloadsViewModel::class.java)
        mainActivityViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        initAdapter(null)
        //initAdapter1(data)
        mainActivityViewModel.progressWorkInfoItems.observe(viewLifecycleOwner, progressObserver())
    }

    private fun initAdapter(list: List<WorkInfo>?) {
        if (adapter == null) {
            adapter = YTDownloadAdapter(list, requireContext(), this)
            _binding.recyclerView.adapter = adapter
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
            val isRunning =listOfWorkInfo.any { it.state == WorkInfo.State.RUNNING }
            _binding.textDownload.visibility = if(isRunning) View.GONE else View.VISIBLE

        }
    }
}