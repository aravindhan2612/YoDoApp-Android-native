package com.example.ytsample.ui.downloads

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.util.forEach
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ytsample.databinding.DownloadsFragmentBinding
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.entities.YtFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadsFragment : Fragment() {


    private lateinit var viewModel: DownloadsViewModel

    private lateinit var _binding: DownloadsFragmentBinding

    private var adapter: YTAdapter? = null

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
        viewModel.responseResult.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                viewModel.extractUrl(it, requireContext())
            }
        })
        viewModel.text.observe(viewLifecycleOwner, Observer {
            _binding.progressHorizontal.visibility = View.GONE
            val ytlist = ArrayList<YtFile>()
            it?.let { ytMetaData ->
                ytMetaData.list.forEach { key, value ->
                    ytlist.add(value)
                }
                initAdapter(ytlist, ytMetaData.meta)
            }
        })
        _binding.progressHorizontal.visibility = View.VISIBLE
        viewModel.getRequest(requireContext(), "https://youtu.be/8F2s8ivKXNY", this)
    }

    fun initAdapter(list: ArrayList<YtFile>, meta: VideoMeta?) {
        _binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = YTAdapter(list, meta, requireContext(), this)
        _binding.recyclerView.adapter = adapter
    }

    private fun loadWebView(s: String) {
        _binding.webviewDownload.webViewClient = webClient
        _binding.webviewDownload.webChromeClient = chromeClient
        _binding.webviewDownload.settings.loadWithOverviewMode = true
        _binding.webviewDownload.settings.setSupportZoom(true)
        _binding.webviewDownload.settings.javaScriptEnabled = true
        _binding.webviewDownload.loadUrl(s)

    }

    private val chromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            _binding.progressHorizontal.visibility = View.VISIBLE
            _binding.progressHorizontal.progress = newProgress
        }
    }


    private val webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            view?.loadUrl(request?.url.toString())
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            _binding.progressHorizontal.visibility = View.GONE
        }

    }

    fun downloadVideo(youtubeDlUrl: String?, downloadTitle: String?, fileName: String?) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val uri = Uri.parse(youtubeDlUrl)
            val request = DownloadManager.Request(uri)
            request.setTitle(downloadTitle)

            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val manager = requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        }
    }

}