package com.dynamic.island.oasis.ui.main

import android.content.IntentFilter
import org.koin.android.ext.android.inject
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.data.UpdateManager
import com.dynamic.island.oasis.databinding.ActivityMainBinding
import com.dynamic.island.oasis.ui.apps.AppsViewModel
import com.dynamic.island.oasis.ui.home.HomeViewModel
import com.dynamic.island.oasis.ui.permissions.PermissionsViewModel
import com.dynamic.island.oasis.ui.settings.SettingsViewModel
import com.dynamic.island.oasis.util.ext.createReceiver
import com.dynamic.island.oasis.util.ext.destroyReceiver
import com.dynamic.island.oasis.util.ext.statusBarColor
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    private var navController: NavController? = null
    private var binding: ActivityMainBinding? = null
    val viewModel by viewModel<MainViewModel>()
    private val settingsViewModel by viewModel<SettingsViewModel>()
    private val appsViewModel by viewModel<AppsViewModel>()
    private val permissionsViewModel by viewModel<PermissionsViewModel>()
    private val homeViewModel by viewModel<HomeViewModel>()
    private val adSource by inject<AdSource>()
    private val update by inject<UpdateManager>()
    private val updateIntentListener =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            viewModel.onUpdateResult(result)
        }
    private val updateInstallListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            update.onUpdateDownloaded(binding?.root)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adSource.createReceiver()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView()
        initViewModels()
        setupNavigation()
        setupObservers()
        checkUpdate()

        createReceiver(viewModel.subReceiver, IntentFilter().apply {
            addAction(Constants.ACTION_SUBSCRIPTION_ACTIVATED)
            addAction(Constants.ACTION_SUBSCRIPTION_DEACTIVATED)
            addAction(Constants.ACTION_SUBSCRIPTIONS_LOADED)
            addAction(Constants.ACTION_BANNER_LOADED)
            addAction(Constants.ACTION_INTER_LOADED)
        })
        createReceiver(viewModel.connReceiver, IntentFilter().apply {
            addAction(Constants.ACTION_CONNECTIVITY)
        })
        adSource.showBanner(binding?.banner)
    }

    fun currentFragment() =
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.fragments?.get(
            0
        )


    private fun checkUpdate() {
        update.onCreate(updateIntentListener, updateInstallListener)
    }

    override fun onDestroy() {
        destroyReceiver(viewModel.subReceiver)
        destroyReceiver(viewModel.connReceiver)
        adSource.onDestroy()
        update.onDestroy(updateInstallListener)
        super.onDestroy()
    }

    fun showPaywall() {
        viewModel.showPaywall()
    }

    private fun initViewModels() {
        settingsViewModel.init()
        appsViewModel.init(this)
        permissionsViewModel.init()
        homeViewModel.init()
    }

    private fun setupObservers() {
        viewModel.showBanner.observe(this) {
            adSource.showBanner(binding?.banner)
        }
        viewModel.statusBarColor.observe(this) {
            statusBarColor(it)
        }
        viewModel.sendBroadcast.observe(this) {
            sendBroadcast(it)
        }
        viewModel.showDestination.observe(this) { destination ->
            navController?.navigate(destination)
        }
    }

    private fun setContentView() {
        val mBinding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
                .apply {
                    lifecycleOwner = this@MainActivity
                    vm = viewModel
                }
        binding = mBinding
    }

    override fun onResume() {
        super.onResume()
        update.onResume(binding?.root)
        viewModel.loadSubscription()
    }

    private fun setupNavigation() {
        val mNavController = findNavController(R.id.nav_host_fragment)
        mNavController.setGraph(R.navigation.nav_graph)
        navController = mNavController

        binding?.let { binding ->
            binding.bottomNav.itemIconTintList = null
            binding.bottomNav.setupWithNavController(mNavController)

            binding.bottomNav.menu.forEach {
                binding.bottomNav.findViewById<View>(it.itemId).setOnLongClickListener {
                    true
                }
            }

            val leftToRight = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(android.R.anim.slide_in_left)
                .setPopExitAnim(android.R.anim.slide_out_right)
                .build()

            val rightToLeft = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(android.R.anim.slide_in_left)
                .setExitAnim(android.R.anim.slide_out_right)
                .setPopEnterAnim(R.anim.slide_in_right)
                .setPopExitAnim(R.anim.slide_out_left)
                .build()

            binding.bottomNav.setOnItemSelectedListener { item ->
                val current = mNavController.currentDestination?.id
                when (item.itemId) {
                    R.id.homeFragment -> {
                        mNavController.navigate(item.itemId, null, rightToLeft)
                    }

                    R.id.settingsFragment -> {
                        val options =
                            if (current == null || current == R.id.homeFragment) leftToRight else rightToLeft
                        mNavController.navigate(item.itemId, null, options)
                    }

                    R.id.infoFragment -> {
                        mNavController.navigate(item.itemId, null, leftToRight)
                    }
                }

                true
            }
        }


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onPermissionResult(requestCode, permissions, grantResults)
    }
}