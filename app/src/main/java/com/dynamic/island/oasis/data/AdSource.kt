package com.dynamic.island.oasis.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.HandlerThread
import android.view.ViewGroup
import android.widget.FrameLayout
import com.dynamic.island.oasis.BuildConfig
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.ui.main.MainActivity
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.createReceiver
import com.dynamic.island.oasis.util.ext.destroyReceiver
import com.dynamic.island.oasis.util.ext.safeLaunch
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdSource(private val context: Context, private val prefs: PrefsUtil) {
    private var initialized = false
    var interstitial: Boolean = false
        private set
    private var interstitialAd: InterstitialAd? = null
    var bannerAd: AdView? = null
        private set
    private var fails: Int = 0




    companion object {
        const val CODE_NO_FILL = 3
    }

    private var job: Job? = null


    private fun startLoading()= CoroutineScope(Dispatchers.IO).launch{
        while(this.isActive){
            if (interstitialAd == null) {
                withContext(Dispatchers.Main) {loadInterstitial() }
            }
            if (bannerAd == null) {
                withContext(Dispatchers.Main) {loadBanner() }
            }

            delay(Constants.AD_LOAD_DELAY)
        }
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.ACTION_SUBSCRIPTION_DEACTIVATED) {
                onCreate()
            } else if (intent?.action == Constants.ACTION_SUBSCRIPTION_ACTIVATED) {
                onDestroy()
            }
        }
    }

    init {
        onCreate()
    }


    fun createReceiver() {
        context.createReceiver(receiver, IntentFilter().apply {
            addAction(Constants.ACTION_SUBSCRIPTION_ACTIVATED)
            addAction(Constants.ACTION_SUBSCRIPTION_DEACTIVATED)
        })
    }




    private val fullscreenCallback = object : FullScreenContentCallback() {
        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            super.onAdFailedToShowFullScreenContent(p0)
            Logs.ads("onAdFailedToShowFullScreenContent; message=${p0.message}")

            interstitial = false
        }

        override fun onAdImpression() {
            super.onAdImpression()
            if (!BuildConfig.DEBUG) context.analyticsEvent("interstitial_impression")
        }

        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            interstitial = true
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            interstitial = false
        }
    }


    private fun onCreate() {
        if (prefs.subscription() || initialized) return

        //            val deviceId = context.androidId().md5().toUpperCase(Locale.US)
//            Logs.ads(deviceId)
        val config = RequestConfiguration.Builder()
            .setTestDeviceIds(
                listOf(
                    AdRequest.DEVICE_ID_EMULATOR,
                    "1E2C6B4C359BE28E5E9C91073D650A55"
                )
            )
            .build()
        MobileAds.setRequestConfiguration(config)
        MobileAds.initialize(context)
        job = startLoading()
        initialized = true
    }
    fun onDestroy() {
        if (!initialized) return
        initialized = false
        job?.cancel()
        job = null
        context.destroyReceiver(receiver)
    }


    private fun loadBanner() {
        val banner = AdView(context)
        banner.setAdSize(AdSize.SMART_BANNER)
        banner.adUnitId = BuildConfig.bannerId
        banner.loadAd(AdRequest.Builder().build())
        banner.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                if (error.code == CODE_NO_FILL) {
                    if (!BuildConfig.DEBUG) context.analyticsEvent("banner_no_fill")
                } else {
                    if (!BuildConfig.DEBUG) context.analyticsEvent("banner_load_failed")
                }

                bannerAd = null
                fails++
            }

            override fun onAdImpression() {
                super.onAdImpression()
                if (!BuildConfig.DEBUG) context.analyticsEvent("banner_impression")
            }

            override fun onAdOpened() {
                super.onAdOpened()
                if (!BuildConfig.DEBUG) context.analyticsEvent("banner_opened")
            }


            override fun onAdLoaded() {
                super.onAdLoaded()
                if (!BuildConfig.DEBUG) context.analyticsEvent("banner_load_success")
            }
        }
        bannerAd = banner
        context.sendBroadcast(Intent(Constants.ACTION_BANNER_LOADED))
    }


    private fun loadInterstitial() {
        val request = AdManagerAdRequest.Builder().build()
        val adUnit = BuildConfig.interstitialId
        AdManagerInterstitialAd.load(
            context,
            adUnit,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    if (error.code == CODE_NO_FILL) {
                        if (!BuildConfig.DEBUG) context.analyticsEvent("interstitial_no_fill")
                    } else {
                        if (!BuildConfig.DEBUG) context.analyticsEvent("interstitial_load_failed")
                    }

                    fails++
                    interstitialAd = null
                }


                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    if (!BuildConfig.DEBUG) context.analyticsEvent("interstitial_load_success")
                    context.sendBroadcast(Intent(Constants.ACTION_INTER_LOADED))
                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = fullscreenCallback

                }
            })
    }


    fun showInterstitial(activity: MainActivity, force: Boolean = false): Boolean {
        if (prefs.subscription()) return false

        val current = activity.currentFragment()
        val showPromotion = activity.viewModel.canShowPromotion(current)
        if (!showPromotion && !force) return false

        return if (interstitialAd != null) {
            if (!BuildConfig.DEBUG) context.analyticsEvent("show_interstitial")
            interstitialAd?.show(activity)
            interstitial = true
            interstitialAd = null
            true
        } else {
            false
        }
    }


    //https://developers.google.com/admob/android/banner
    fun showBanner(container: FrameLayout?) {
        if (container == null) return
        val mBanner = bannerAd ?: return


        if (mBanner.parent != null) {
            (mBanner.parent as ViewGroup).removeView(mBanner)
        }
        if (!BuildConfig.DEBUG) context.analyticsEvent("show_banner")
        container.addView(mBanner)
    }


}