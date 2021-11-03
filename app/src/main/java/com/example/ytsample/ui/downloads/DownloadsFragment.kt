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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.work.WorkInfo
import com.example.ytsample.MainActivity
import com.example.ytsample.adapter.YTDownloadAdapter
import com.example.ytsample.databinding.DownloadsFragmentBinding
import com.example.ytsample.entities.ProgressState
import com.example.ytsample.utils.MainViewModel

class DownloadsFragment : Fragment() {

    private lateinit var viewModel: DownloadsViewModel
    private lateinit var _binding: DownloadsFragmentBinding
    private lateinit var mainActivity: MainActivity
    val args: DownloadsFragmentArgs by navArgs()
    private var progressStateList = ArrayList<ProgressState>()
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
        viewModel = ViewModelProvider(this).get(DownloadsViewModel::class.java)
        mainActivityViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
//        LiveDataHelper.instance?.observePercentage()?.observe(viewLifecycleOwner, Observer {
//            if (it != null && it.isFinished == false) {
//                if (it.percent in 0..100) {
//                    _binding.progressHorizontal.visibility = View.VISIBLE
//                    _binding.progressHorizontal.progress = it.percent!!
//                    _binding.textDownload.text =
//                        it.percent.toString() + "% downloading: " + it.onProgress + " mb / " + it.length + " mb"
//                }
//                if (it.percent == 100) {
//                    Snackbar.make(view, " Download completed ", Snackbar.LENGTH_SHORT)
//                        .show()
//                    _binding.progressHorizontal.visibility = View.GONE
//                    _binding.textDownload.visibility = View.GONE
//                }
//            } else {
//                _binding.textDownload.text = "No downloads"
//            }
//        })
        initAdapter(null)
        mainActivityViewModel.progressWorkInfoItems.observe(viewLifecycleOwner, progressObserver())
    }

    private fun initAdapter(list: List<WorkInfo>?) {
        if (adapter == null) {
            adapter = YTDownloadAdapter(list, requireContext(), this)
            _binding.recyclerView.adapter = adapter
            _binding.recyclerView.apply {
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
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
                return@Observer
            }

            listOfWorkInfo?.let {
                _binding.recyclerView.adapter = adapter
                adapter?.refreshList(it as ArrayList<WorkInfo>)
            }
            println("******listOfWorkInfo size " + listOfWorkInfo.size)
//            forEach { workInfo ->
//
//                if (WorkInfo.State.RUNNING == workInfo.state) {
//                    val progress = workInfo.progress.getInt(Constants.PROGRESS, 0)
//                    _binding.progressHorizontal.visibility = View.VISIBLE
//                    _binding.progressHorizontal.progress = progress
//                    _binding.textDownload.text = "$progress%"
//
//                }
//            }

        }
    }

    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->

            // Note that these next few lines grab a single WorkInfo if it exists
            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]
        }
    }

}