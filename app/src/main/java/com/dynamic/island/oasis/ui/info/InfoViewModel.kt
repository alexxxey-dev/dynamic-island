package com.dynamic.island.oasis.ui.info

import android.content.Intent
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.BillingUtil
import com.dynamic.island.oasis.data.MyConfig
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.ui.paywall.PaywallViewModel
import com.dynamic.island.oasis.util.LockGuide
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.launch

class InfoViewModel(
    private val prefs: PrefsUtil,
    private val lock:LockGuide
): ViewModel() {
    val subscription = MutableLiveData<Boolean>()
    val infoDisplayed = MutableLiveData<Boolean>()
    val showUrl = SingleLiveEvent<String>()
    val shareApp = SingleLiveEvent<Unit>()
    val showDestination = SingleLiveEvent<Int>()
    val launchEmail = SingleLiveEvent<Unit>()
    val showInfo = SingleLiveEvent<Boolean>()
    val sendBroadcast = SingleLiveEvent<Intent>()
    val showPaywall = SingleLiveEvent<Unit>()
    fun loadSubscription(){
        subscription.value = prefs.subscription()
    }

    fun onReceive(intent: Intent){
        if(intent.action == Constants.ACTION_SUBSCRIPTION_DEACTIVATED || intent.action == Constants.ACTION_SUBSCRIPTION_DEACTIVATED){
            loadSubscription()
        }
    }

    fun showLockGuide(){
        showUrl.value = lock.loadUrl()
    }
    fun onAdsClicked(view: View){
        viewModelScope.safeLaunch {
            showPaywall.value = Unit
        }

    }

    fun onShareClicked(view:View){
        view.context.analyticsEvent("share_clicked")
        shareApp.value = Unit
    }


    fun onRateClicked(view:View){
       showDestination.value = R.id.dialogRate
    }


    fun onSupportClicked(view:View){
        view.context.analyticsEvent("support_clicked")
        launchEmail.value = Unit
    }


    fun onShowClicked(view:View){
        val mShow = !(infoDisplayed.value ?: false)
        if(mShow) view.context.analyticsEvent("show_info_not_working")
        infoDisplayed.value = mShow
        showInfo.value = mShow
    }

    fun onPolicyClicked(view:View){
        view.context.analyticsEvent("policy_clicked")
        showUrl.value = Constants.URL_POLICY
    }

    fun onTermsClicked(view:View){
        view.context.analyticsEvent("terms_clicked")
        showUrl.value = Constants.URL_TERMS
    }


}