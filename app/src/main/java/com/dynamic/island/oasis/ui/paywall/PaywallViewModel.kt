package com.dynamic.island.oasis.ui.paywall

import android.app.Activity
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.BillingUtil
import com.dynamic.island.oasis.data.MyConfig
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.Subscription
import com.dynamic.island.oasis.util.NetworkUtil
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val billing: BillingUtil,
    private val config: MyConfig
) : ViewModel() {
    val features = listOf(
        R.string.subscription_reason_1,
        R.string.subscription_reason_2,
        R.string.subscription_reason_3
    )
    val popBackStack = SingleLiveEvent<Unit>()
    val selectedSubscription = MutableLiveData<Subscription?>()
    val allSubscriptions = MutableLiveData<List<Subscription>>()
    val makePurchase = SingleLiveEvent<Subscription>()
    val showMessage = SingleLiveEvent<Int>()
    val showUrl = SingleLiveEvent<String>()
    val profit = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val showInterstitial = SingleLiveEvent<Unit>()
    init {
        val subscriptions = billing.subscriptions
            .sortedByDescending { it.id == Constants.SUB_YEARLY || it.id == Constants.SUB_YEARLY_TRIAL }

        allSubscriptions.value = subscriptions
        selectedSubscription.value = subscriptions.firstOrNull()
        loadProfit(subscriptions)

    }

    private fun loadProfit(subscriptions: List<Subscription>) {
        val subYear =
            subscriptions.find { it.id == Constants.SUB_YEARLY || it.id == Constants.SUB_YEARLY_TRIAL }
        val subMonth =
            subscriptions.find { it.id == Constants.SUB_MONTHLY || it.id == Constants.SUB_MONTHLY_TRIAL }
        if (subYear == null || subMonth == null) return

        val priceA = subYear.price / 12
        val priceB = subMonth.price
        val percent = ((1 - priceA / priceB) * 100).toInt()
        if (percent < 0) return
        profit.value = "$percent%"
    }

    fun showPolicy() {
        showUrl.value = Constants.URL_POLICY
    }

    fun showTerms() {
        showUrl.value = Constants.URL_TERMS
    }

    fun restorePurchases() = viewModelScope.launch{
        showMessage.value = R.string.checking
        val success = billing.restorePurchases()
        if(!success) {
            showMessage.value = R.string.error
            return@launch
        }
        showMessage.value = R.string.success
        popBackStack.value = Unit
    }
    fun close() {
        showInterstitial.value = Unit
        popBackStack.value = Unit
    }

    fun onSubscriptionClicked(pos:Int) {
        val mList = allSubscriptions.value ?: emptyList()
        val subscription = if(pos<mList.size) mList[pos] else return
        selectedSubscription.value = subscription
    }

    fun onSubscriptionClicked(subscription: Subscription) {
        selectedSubscription.value = subscription
    }

    fun makePurchase(activity: Activity, fragmentId: String, subscription: Subscription) {
        loading.value = true
        billing.purchase(
            activity,
            subscription.packageId,

            onSuccess = {
                loading.value = false
                showMessage.value = R.string.success
                popBackStack.value = Unit
            },
            onError = { userCancelled ->
                loading.value = false
            }
        )
    }

    fun onBuyClicked() {
        val mSelected = selectedSubscription.value ?: return
        makePurchase.value = mSelected
    }
}