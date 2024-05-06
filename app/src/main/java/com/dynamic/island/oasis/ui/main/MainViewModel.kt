package com.dynamic.island.oasis.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.data.BillingUtil
import com.dynamic.island.oasis.data.MyConfig
import com.dynamic.island.oasis.ui.home.HomeFragment
import com.dynamic.island.oasis.ui.info.InfoFragment
import com.dynamic.island.oasis.ui.settings.SettingsFragment
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.ui.onboarding.OnboardingFragment
import com.dynamic.island.oasis.ui.onboarding.OnboardingPageFragment
import com.dynamic.island.oasis.ui.onboarding.OnboardingPermissionsFragment
import com.dynamic.island.oasis.ui.paywall.PaywallAFragment
import com.dynamic.island.oasis.ui.paywall.PaywallBFragment
import com.dynamic.island.oasis.ui.paywall.PaywallCFragment
import com.dynamic.island.oasis.util.NetworkUtil
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.safeLaunch

class MainViewModel(
    private val prefs: PrefsUtil,
     val adSource: AdSource,
    private val billing: BillingUtil,
    private val config: MyConfig,
    private val network:NetworkUtil
) : ViewModel() {
    val bottomNavVisible = MutableLiveData<Boolean>()
    val statusBarColor = MutableLiveData<Int>()
    val sendBroadcast = SingleLiveEvent<Intent>()
    val showDestination = SingleLiveEvent<Int>()
    val subscription = MutableLiveData<Boolean>()
    val isOnboarding = MutableLiveData<Boolean>()
    val isPaywall = MutableLiveData<Boolean>()
    val connected = MutableLiveData<Boolean>()
    val showBanner = SingleLiveEvent<Unit>()

     val subReceiver = object:BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == Constants.ACTION_SUBSCRIPTION_ACTIVATED || intent?.action ==Constants.ACTION_SUBSCRIPTION_DEACTIVATED || intent?.action ==Constants.ACTION_SUBSCRIPTIONS_LOADED){
                loadSubscription()
            } else if(intent?.action == Constants.ACTION_BANNER_LOADED){
                showBanner.value = Unit
            }
        }
    }

    private var wasDisconnected = false

     val connReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Constants.ACTION_CONNECTIVITY) {
                return
            }

            val status = network.getConnectivityStatusString()
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                onNetworkDisconnected()
            } else if(network.isConnected) {
                onNetworkConnected()
            }
        }

    }

    private fun onNetworkDisconnected(){
        wasDisconnected = true
        connected.value = false
    }


    private fun onNetworkConnected(){
        if (wasDisconnected) {
            wasDisconnected = false
        }
        connected.value = true
        if(!billing.initialized) {
            billing.init()
        }
    }
    fun loadSubscription() {
        subscription.value = prefs.subscription()
    }

    fun onUpdateResult(result:ActivityResult){
        if(result.resultCode!= AppCompatActivity.RESULT_OK){
            Logs.log("update flow failed")
        }
    }
    fun onFragmentCreated(fragment: Fragment) = viewModelScope.safeLaunch {
        updateStatusBarColor(fragment)
        updateBottomNavVisibility(fragment)
        isOnboarding.value = fragment is OnboardingFragment || fragment is OnboardingPageFragment || fragment is OnboardingPermissionsFragment
        isPaywall.value =fragment is PaywallAFragment || fragment is PaywallBFragment || fragment is PaywallCFragment
        loadSubscription()
        val showInterstitial = showInterstitial(fragment)
        if (!showInterstitial) showRate(fragment)
    }

    fun showPaywall() {
        if (prefs.subscription()) return
        if (billing.subscriptions.isEmpty()) return
        val destination = when (config.loadPaywallVersion()) {
            Constants.PAYWALL_A -> {
                R.id.action_paywallAFragment
            }
            Constants.PAYWALL_B -> {
                R.id.action_paywallBFragment
            }
            Constants.PAYWALL_C -> {
                R.id.action_paywallCFragment
            }
            else -> {
                R.id.action_paywallAFragment
            }
        }

        showDestination.value = destination
    }

    fun canShowPromotion(fragment:Fragment?) :Boolean{
        if(fragment ==null) return true
        if (fragment is OnboardingFragment || fragment is OnboardingPageFragment || fragment is OnboardingPermissionsFragment) return false
        if (fragment is PaywallAFragment || fragment is PaywallBFragment || fragment is PaywallCFragment) return false
        return true
    }
    private fun showInterstitial(fragment: Fragment): Boolean {
        if(!canShowPromotion(fragment)) return false

        prefs.interCount(prefs.interCount() + 1)
        if (prefs.interCount() == 0) return false
        if (prefs.interCount() % Constants.INTERSTITIAL_FREQ != 0) return false

        return adSource.showInterstitial(fragment.requireActivity() as MainActivity)
    }

    private fun showRate(fragment: Fragment): Boolean {
        if(!canShowPromotion(fragment)) return false
        if(adSource.interstitial) return false

        prefs.rateCount(prefs.rateCount() + 1)

        val rateCount = prefs.rateCount()
        if (rateCount == 0) return false
        if (rateCount % Constants.RATE_FREQ != 0) return false

        showDestination.value = R.id.action_dialogRate
        return true
    }


    private fun updateStatusBarColor(
        fragment: Fragment
    ) {
        val isWhite = fragment is HomeFragment || fragment is InfoFragment
        val isPaywallB = fragment is PaywallBFragment
        statusBarColor.value = if (isWhite) {
            R.color.white
        } else if (isPaywallB) {
            R.color.pink_2
        } else {
            R.color.white_pink
        }
    }

    private fun updateBottomNavVisibility(
        fragment: Fragment
    ) {
        val isVisible = fragment is InfoFragment
                || fragment is HomeFragment
                || fragment is SettingsFragment
        bottomNavVisible.value = isVisible
    }

    fun onPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        if(requestCode!=Constants.CODE_PHONE_PERMISSION) return
        val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if(!granted) return
        sendBroadcast.value = Intent(Constants.ACTION_PHONE_PERMISSION)
    }
}