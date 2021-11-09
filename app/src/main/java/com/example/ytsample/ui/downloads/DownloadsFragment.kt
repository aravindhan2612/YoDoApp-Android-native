package com.example.ytsample.ui.downloads

import android.content.ContentValues
import android.content.Context
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.work.WorkInfo
import com.example.ytsample.MainActivity
import com.example.ytsample.adapter.YTDownloadAdapter
import com.example.ytsample.databinding.DownloadsFragmentBinding
import com.example.ytsample.entities.DownloadResult
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.utils.MainViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import kotlin.math.roundToInt

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

    private fun initAdapter1(data: ArrayList<DownloadedData>) {
        if (adapter == null) {
//            adapter = YTDownloadAdapter2(data){
//                manageClickAdapter(it)
//            }
            _binding.recyclerView.adapter = adapter
            _binding.recyclerView.apply {
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
    }

    private fun manageClickAdapter(it: DownloadedData) {
        var output: OutputStream? = null
            var file: File? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = requireContext().contentResolver
                val values = ContentValues()
                values.put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    if (it.isVideo == true) "file_${System.currentTimeMillis()}.mp4" else "file_${System.currentTimeMillis()}.mp3"
                )
                values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )
                val uri =
                    resolver.insert(MediaStore.Files.getContentUri("external"), values)

                // Output stream to write file
                file = File(uri!!.toString())
                output = uri?.let { resolver.openOutputStream(it) }
            } else {
                file = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ),
                    if (it.isVideo == true) "file_${System.currentTimeMillis()}.mp4" else "file_${System.currentTimeMillis()}.mp3"
                )
                // Output stream to write file
                output = FileOutputStream(file, true)
            }
        when {
            it.isDownloading -> {
                //Do nothing
            }
            else -> {
                try {
                    downloadWithFlow(it,file)
                } catch (e: Exception) {
                    //generic error while downloading
                }
            }
        }
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
                _binding.textDownload.visibility = View.VISIBLE
                _binding.recyclerView.visibility = View.GONE
                return@Observer
            }

            listOfWorkInfo?.let {
                _binding.recyclerView.visibility = View.VISIBLE
                _binding.textDownload.visibility = View.GONE
                _binding.recyclerView.adapter = adapter
                adapter?.refreshList(it as ArrayList<WorkInfo>)
            }

        }
    }
    private fun downloadWithFlow(dummy: DownloadedData,file:File) {

        CoroutineScope(Dispatchers.IO).launch {
            var ktor = HttpClient(Android)
            ktor.downloadFile(file,dummy.youtubeDlUrl!!).collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        is DownloadResult.Success -> {
                      //      adapter?.setDownloading(dummy, false)
                        }
                        is DownloadResult.Error -> {
                           // adapter?.setDownloading(dummy, false)
                            Toast.makeText(requireContext(), "Error while downloading ${dummy.downloadTitle}", Toast.LENGTH_LONG).show()
                        }
                        is DownloadResult.Progress -> {
                           // adapter?.setProgress(dummy, it.progress)
                        }
                    }
                }
            }
        }
    }

    private suspend fun HttpClient.downloadFile(file: File, url: String): Flow<DownloadResult> {
        return flow {
            val response = call {
                url(url)
                method = HttpMethod.Get
            }.response
            val data = ByteArray(response.contentLength()!!.toInt())
            var offset = 0
            do {
                val currentRead = response.content.readAvailable(data, offset, data.size)
                offset += currentRead
                val mb = offset
                val progress = (mb * 100f / data.size).roundToInt()
                emit(DownloadResult.Progress(progress,mb))
            } while (currentRead > 0)
            response.close()
            if (response.status.isSuccess()) {
                file.writeBytes(data)
                emit(DownloadResult.Success)
            } else {
                emit(DownloadResult.Error("File not downloaded"))
            }
        }
    }
}