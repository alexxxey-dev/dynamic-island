package com.dynamic.island.oasis.ui.splash

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat.animate
import androidx.lifecycle.lifecycleScope
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.data.BillingUtil
import com.dynamic.island.oasis.data.MyConfig
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.util.ScreenMetricsCompat
import com.dynamic.island.oasis.ui.main.MainActivity
import com.dynamic.island.oasis.util.NetworkUtil
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.createReceiver
import com.dynamic.island.oasis.util.ext.destroyReceiver
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val billing by inject<BillingUtil>()
    private val prefs by inject<PrefsUtil>()
    private val network by inject<NetworkUtil>()
    private val window by inject<WindowManager>()
    private var count = 0
    private val subsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showNext()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        setContentView(R.layout.activity_splash)
        analyticsEvent("open_splash_screen")

        lifecycleScope.safeLaunch {
            animate()
            if(showNext()) return@safeLaunch
            createReceiver(subsReceiver, IntentFilter().apply {
                addAction(Constants.ACTION_SUBSCRIPTIONS_LOADED)
                addAction(Constants.ACTION_CONFIG_FETCH)
            })
        }
    }



    private  fun loadSubs():Boolean{
        return prefs.subscription() || billing.subscriptions.isNotEmpty() || !network.isConnected
    }

    private suspend fun animate() = suspendCancellableCoroutine { cont->
        val size = resources.getDimension(R.dimen.splash_logo)
        val screenHeight = ScreenMetricsCompat.screenSize(window).height.toFloat()
        val logo = findViewById<View>(R.id.logo)

        val finalY = screenHeight / 2 - size
        logo.animate()
            .setInterpolator(BounceInterpolator())
            .y(finalY)
            .setDuration(800)
            .withEndAction {
                logo.y = finalY
               if(cont.isActive) cont.resume(Unit)
            }
            .start()
    }


    private fun showNext():Boolean {
        if(!loadSubs() || count > 0) {
           return false
        }
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
        count ++
        return true
    }

    override fun onDestroy() {
        destroyReceiver(subsReceiver)
        super.onDestroy()

    }


}