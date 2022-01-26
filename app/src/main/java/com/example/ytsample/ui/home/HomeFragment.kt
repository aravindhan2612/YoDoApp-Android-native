package com.example.ytsample.ui.home

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.ytsample.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar


class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var _binding: FragmentHomeBinding

    private lateinit var mainActivity: MainActivity

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
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                initAllData()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
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
                    val action =
                        HomeFragmentDirections.actionNavigationHomeToYtBottomSheetFragment(_binding.editUrl.text.toString(),false)
                    findNavController().navigate(action)
                }
            }
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

}