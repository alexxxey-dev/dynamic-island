package com.dynamic.island.oasis.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.util.DiParamsProvider
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.dynamic_island.service.MainService
import com.dynamic.island.oasis.dynamic_island.service.ServiceWrapper
import com.dynamic.island.oasis.util.LockGuide
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.doVibration
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.launch

class HomeViewModel(
    private val prefs: PrefsUtil,
    private val permissions: PermissionsUtil,
    private val diParams: DiParamsProvider,
    private val vibrator: Vibrator,
    private val lock:LockGuide
) :
    ViewModel() {
    private var paywallCount = 0
    val showDestination = SingleLiveEvent<Int>()
    val showDestinationBundle = SingleLiveEvent<Pair<Int, Bundle>>()
    val diNotch = MutableLiveData<Boolean>()
    val diX = MutableLiveData<Int>()
    val diY = MutableLiveData<Int>()
    val diWidth = MutableLiveData<Int>()
    val diHeight = MutableLiveData<Int>()
    val diEnabled = MutableLiveData<Boolean>()

    val subscription = MutableLiveData<Boolean>()
    val sendBroadcast = SingleLiveEvent<Intent>()
    val showInterstitial = SingleLiveEvent<Unit>()
    val showPaywall = SingleLiveEvent<Unit>()

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                Constants.ACTION_SUBSCRIPTION_DEACTIVATED,Constants.ACTION_SUBSCRIPTION_ACTIVATED->loadSubscription()
            }

        }
    }



    fun loadLockGuide() = lock.loadUrl()

    fun loadSubscription() {
        subscription.value = prefs.subscription()
    }


    fun showPaywall() {
        if (paywallCount > 0) return
        if (prefs.showOnboarding()) return
        showPaywall.value = Unit
        paywallCount++

    }

    fun showOnboarding() {
        val onboarding = prefs.showOnboarding()
        if (onboarding) showDestination.value = R.id.action_onboardingFragment
    }

    fun init() = viewModelScope.launch{

        val mDiParams = diParams.providePercent()
        diX.value = mDiParams.x
        diY.value = mDiParams.y
        diWidth.value = mDiParams.width
        diHeight.value = mDiParams.height
        diEnabled.value = prefs.serviceEnabled()
        diNotch.value = prefs.isBackgroundNotch()
    }




    fun setBackgroundNotch(view: View, notch: Boolean) {
        prefs.isBackgroundNotch(notch)
        diNotch.value = notch
        sendBroadcast.value = Intent(Constants.ACTION_UPDATE_BG)
        view.context.analyticsEvent(
            "di_set_background_notch", bundleOf(
                "is_notch" to notch
            )
        )
    }

    val updateX = fun(progress: Int, fromUser: Boolean) {
        if (fromUser) {
            diX.value = progress
            diParams.update(xPercent = progress / 100f)
        }
    }

    val updateY = fun(progress: Int, fromUser: Boolean) {
        if (fromUser) {
            diY.value = progress
            diParams.update(yPercent = progress / 100f)
        }

    }
    val updateHeight = fun(progress: Int, fromUser: Boolean) {
        if (fromUser) {
            diHeight.value = progress
            diParams.update(heightPercent = progress / 100f)
        }
    }
    val updateWidth = fun(progress: Int, fromUser: Boolean) {
        if (fromUser) {
            diWidth.value = progress
            diParams.update(widthPercent = progress / 100f)
        }
    }


    fun onSubscriptionClicked(view: View) {
        viewModelScope.safeLaunch {
            showPaywall.value = Unit
        }

    }

    fun resetSettings() {
        diX.value = (Constants.DEFAULT_X * 100).toInt()
        diY.value = (Constants.DEFAULT_Y * 100).toInt()
        diWidth.value = (Constants.DEFAULT_WIDTH * 100).toInt()
        diHeight.value = (Constants.DEFAULT_HEIGHT * 100).toInt()
        diParams.update(
            xPercent = Constants.DEFAULT_X,
            yPercent = Constants.DEFAULT_Y,
            widthPercent = Constants.DEFAULT_WIDTH,
            heightPercent = Constants.DEFAULT_HEIGHT
        )
    }


    fun checkCanStart(view: View) {
        val mEnabled = prefs.serviceEnabled()

        if (!permissions.isGranted(PermissionType.DRAW_OVERLAY) && !mEnabled) {
            showDestinationBundle.value = Pair(
                R.id.action_permissionDialog,
                bundleOf(Constants.PARAM_PERMISSION_TYPE to PermissionType.DRAW_OVERLAY)
            )
            return
        }

        if (!permissions.isGranted(PermissionType.NOTIF) && !mEnabled) {
            showDestinationBundle.value = Pair(
                R.id.action_permissionDialog,
                bundleOf(Constants.PARAM_PERMISSION_TYPE to PermissionType.NOTIF)
            )
            return
        }

        if(prefs.showLockDialog() && !mEnabled){
            showDestination.value =R.id.action_dialogLock
            return
        }

        startStop(view)
    }


    fun startStop(view:View){
        val enabled = !prefs.serviceEnabled()
        prefs.serviceEnabled(enabled)
        vibrator.doVibration(Constants.LONG_CLICK_VIBRATION)
        diEnabled.value = enabled

        if (enabled) {
            view.context.analyticsEvent("on_start_clicked")
            MainService.startViaWorker(view.context)
            showInterstitial.value = Unit
        } else {
            MainService.stop(view.context)
            view.context.analyticsEvent("on_stop_clicked")
        }
    }
}