package com.example.ytsample.ui.home

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ytsample.controllers.MainActivity
import com.example.ytsample.R
import com.example.ytsample.callbacks.IDialogListener
import com.example.ytsample.databinding.FragmentHomeBinding
import com.example.ytsample.entities.DownloadedData
import com.example.ytsample.ui.bottomsheet.YtBottomSheetFragment
import com.example.ytsample.utils.Constants
import com.example.ytsample.utils.MainViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File


class HomeFragment : Fragment(), View.OnClickListener,IDialogListener {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var _binding: FragmentHomeBinding
    private var mainActivity: MainActivity? = null
    private var mainViewModel: MainViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                initAllData()
                createFolderDir()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity?
    }

    private fun initAllData() {
        _binding.downloadUrl.setOnClickListener(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                initAllData()
                createFolderDir()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                view?.let { Snackbar.make(it, "permission denied", Snackbar.LENGTH_SHORT) }
            }
        }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.download_url -> {
                closeKeyboard()
                if (_binding.editUrl.text.isNullOrEmpty()) {
                    setErrorTV()
                } else if (!android.util.Patterns.WEB_URL.matcher(_binding.editUrl.text.toString())
                        .matches()
                ) {
                    setErrorTV()
                } else {
                    showYTBottomSheetDialog()
                }
            }
        }
    }

    private fun showYTBottomSheetDialog() {
        val dialogInStack =  this.childFragmentManager.findFragmentByTag(Constants.YT_BOTTOM_SHEET_FRAGMENT_TAG) as YtBottomSheetFragment?
        if (dialogInStack == null) {
            val dialog = YtBottomSheetFragment.newInstance(_binding.editUrl.text.toString())
            dialog.isCancelable = false
            dialog.setDialogListener(this)
            dialog.show(this.childFragmentManager, Constants.YT_BOTTOM_SHEET_FRAGMENT_TAG)
        }
    }

    private fun setErrorTV() {
        _binding.editUrl.error = " please enter valid url"
    }

    private fun closeKeyboard() {
        // this will give us the view
        // which is currently focus
        // in this layout
        val view: View? = requireActivity().currentFocus

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {

            // now assign the system
            // service to InputMethodManager
            val manager: InputMethodManager? = requireActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager?
            manager?.hideSoftInputFromWindow(
                view.windowToken, 0
            )
        }
    }

    private fun createFolderDir() {
        var file: File? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = activity?.contentResolver
            val values = ContentValues()
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/YoDoApp"
            )
            val uri =
                resolver?.insert(MediaStore.Files.getContentUri("external"), values)

            // Output stream to write file uri?.let { resolver.openOutputStream(it) }
            file = uri?.path?.let { File(it) }
        } else {
            file =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/YoDoApp")
            // Output stream to write file
        }
        if (file?.isDirectory == false) {
            file.mkdir()
        }
    }

    fun downloadVideoData(downloadedData: DownloadedData) {
        if (downloadedData.youtubeDlUrl != null) {
            mainViewModel?.downloadvideo(downloadedData)
            val action =
                HomeFragmentDirections.actionYtBottomSheetFragmentToNavigationDownload(
                    downloadedData
                )
            findNavController().navigate(action)
        } else {
            view?.let {
                Snackbar.make(it, "Unable to download video ", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDialogResult(type: Any?) {
        type?.let {
            if (it is DownloadedData){
                downloadVideoData(it)
            }
        }
    }
}