package com.example.ytsample.controllers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkInfo
import com.example.ytsample.R
import com.example.ytsample.databinding.ActivityMainBinding
import com.example.ytsample.ui.home.HomeFragmentDirections
import com.example.ytsample.utils.Constants
import com.example.ytsample.utils.Constants.Companion.TAG_PROGRESS
import com.example.ytsample.utils.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        clearWorkManager()
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                handleSendText(intent) // Handle text being sent
            }
        }
    }

    private fun clearWorkManager() {

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        val wmList  = mainViewModel?.workManager?.getWorkInfosByTag(TAG_PROGRESS)
        val anyWorkIsRunning = wmList?.get()?.any { it.state == WorkInfo.State.RUNNING }
        if (anyWorkIsRunning == false){
            //deleting the finished onetime request workmanger to reduce the list size in download page
            // temporary fix for optimisation permanent fix need to done when it is available is future
            mainViewModel?.workManager?.pruneWork()
        }
    }

    private fun initView() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_download
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            val action =
                HomeFragmentDirections.actionNavigationHomeToYtBottomSheetFragment(it, true)
            findNavController(R.id.nav_host_fragment_activity_main).navigate(action)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                initView()
                handleSendText(intent) // Handle text being sent
            }
        }
        if (intent != null) {
            val data = intent.getStringExtra("data");
            if (data != null) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_download)
            }
        }
    }


}