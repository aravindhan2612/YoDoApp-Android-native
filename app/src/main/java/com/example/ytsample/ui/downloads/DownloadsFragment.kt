package com.example.ytsample.ui.downloads

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.ytsample.MainActivity
import com.example.ytsample.R
import com.example.ytsample.databinding.DownloadsFragmentBinding
import com.example.ytsample.ui.bottomsheet.YtBottomSheetFragmentArgs
import com.example.ytsample.ui.home.LiveDataHelper
import com.example.ytsample.utils.YTNotification
import com.google.android.material.snackbar.Snackbar

class DownloadsFragment : Fragment() {

    private lateinit var viewModel: DownloadsViewModel
    private lateinit var _binding: DownloadsFragmentBinding
    private lateinit var mainActivity: MainActivity
    val args: DownloadsFragmentArgs by navArgs()

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
        LiveDataHelper.instance?.observePercentage()?.observe(viewLifecycleOwner, Observer {
            mainActivity.isWorkInfoRunning = false
            if (it != null && it.isFinished == false) {
                if (it.percent in 0..100) {
                    mainActivity.isWorkInfoRunning = true
                    _binding.progressHorizontal.visibility = View.VISIBLE
                    _binding.progressHorizontal.progress = it.percent!!
                    _binding.textDownload.text =
                        it.percent.toString() + "% downloading: " + it.onProgress + " mb / " + it.length + " mb"
                }
                if (it.percent == 100) {
                    Snackbar.make(view, " Download completed ", Snackbar.LENGTH_SHORT)
                        .show()
                    _binding.progressHorizontal.visibility = View.GONE
                    _binding.textDownload.visibility = View.GONE
                    mainActivity.isWorkInfoRunning = false
                }
            } else {
                _binding.textDownload.text = "No downloads"
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }


}