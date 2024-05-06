package com.dynamic.island.oasis.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.dynamic.island.oasis.BuildConfig
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.repository.SettingsRepository
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safeLaunch
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.purchaseWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.coroutines.resume


class BillingUtil(
    private val context: Context,
    private val prefs: PrefsUtil,
    private val config: MyConfig,
    private val settings: SettingsRepository
) {
    val subscriptions = ArrayList<Subscription>()
    private val packages = ArrayList<Package>()
    var initialized = false
        private set


    init {
        init()
    }
    fun init() {
        CoroutineScope(Dispatchers.IO).safeLaunch {
            try {
                initConfig()
                val success1 = async { loadPackages() }
                val success2 = async { checkSubscription() }
                initialized = success1.await() && success2.await()
            } catch (ex: Exception) {
                ex.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

    suspend fun restorePurchases() = suspendCancellableCoroutine<Boolean> { cont ->
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                logError("Restore purchases error", error)
                if (cont.isActive) cont.resume(value = false)
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                val active =
                    customerInfo.entitlements[Constants.ENTITLEMENT_ID]?.isActive == true
                if (active) onSubscriptionActive()
                if (cont.isActive) cont.resume(value = active)
            }
        })

    }

    fun purchase(
        activity: Activity,
        packageId: String,
        onSuccess: () -> Unit,
        onError: (userCancelled: Boolean) -> Unit
    ) {
        try {
            val mPackage = packages.find { it.identifier == packageId } ?: return
            val params = PurchaseParams.Builder(activity, mPackage).build()
            Purchases.sharedInstance.purchaseWith(
                params,
                onError = { error, userCancelled ->
                    if (error.code != PurchasesErrorCode.PurchaseCancelledError) {
                        logError("Make purchase error", error)
                    }
                    onError(userCancelled)
                },
                onSuccess = { _, customerInfo ->
                    val active =
                        customerInfo.entitlements[Constants.ENTITLEMENT_ID]?.isActive == true
                    if (active) {
                        if (!BuildConfig.DEBUG) context.analyticsEvent("purchase_complete")
                        onSubscriptionActive()
                        onSuccess()
                    } else {
                        if (!BuildConfig.DEBUG) context.analyticsEvent("purchase_error")
                        onError(false)
                    }
                }
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun initConfig() {
        if (BuildConfig.DEBUG) {
            Purchases.logLevel = LogLevel.DEBUG
        }
        val config = PurchasesConfiguration.Builder(context, BuildConfig.BILLING_API_KEY).build()
        Purchases.configure(config)
    }

    private suspend fun checkSubscription() = suspendCancellableCoroutine<Boolean> {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                logError("Get customer info error", error)
                if (it.isActive) it.resume(false)
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                val active =
                    customerInfo.entitlements[Constants.ENTITLEMENT_ID]?.isActive == true
                if (active) onSubscriptionActive() else onSubscriptionInactive()
                if (it.isActive) it.resume(true)
            }
        })
    }


    private fun onSubscriptionActive() {
        prefs.subscription(true)
        context.sendBroadcast(Intent(Constants.ACTION_SUBSCRIPTION_ACTIVATED))
    }

    private fun onSubscriptionInactive() {
        prefs.subscription(false)
        settings.onSubscriptionInactive()
        context.sendBroadcast(Intent(Constants.ACTION_SUBSCRIPTION_DEACTIVATED))
    }

    private fun logError(title: String, error: PurchasesError) {
        val msg =
            "$title; code=${error.code}; message=${error.message}; underlying message=${error.underlyingErrorMessage}"
        Logs.billing(msg)
    }

    private suspend fun loadPackages() = suspendCancellableCoroutine<Boolean> {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                logError("Load offering error", error)
                if (it.isActive) it.resume(false)
            },
            onSuccess = { offerings ->
                val offering = loadOffering(offerings)
                val mPackages = (offering?.availablePackages ?: emptyList())
                val mSubscriptions = mPackages.map { mapToSub(it) }



                packages.clear()
                packages.addAll(mPackages)
                subscriptions.clear()
                subscriptions.addAll(mSubscriptions)

                context.sendBroadcast(Intent(Constants.ACTION_SUBSCRIPTIONS_LOADED))
                if (it.isActive) it.resume(true)
            })
    }

    private fun mapToSub(it: Package): Subscription {
        return Subscription(
            packageId = it.identifier,
            id = it.product.id,
            title = loadTitle(it),
            description = loadDescription(it),
            numberTitle = loadNumberTitle(it),
            priceText = loadPrice(it),
            isTrial = isTrial(it),
            price = it.product.price.amountMicros.toDouble(),
            shortDescription = loadShortDescription(it)
        )
    }


    private fun loadOffering(offerings: Offerings): Offering? {
        val version = config.loadOfferingVersion()
        val mOffering = try {
            offerings.getOffering(version)
        } catch (ex: Exception) {
            ex.printStackTrace()
            offerings.current
        }
        return mOffering
    }

    private fun isTrial(mPackage: Package): Boolean {
        return when (mPackage.product.id) {
            Constants.SUB_MONTHLY_TRIAL, Constants.SUB_YEARLY_TRIAL -> true
            else -> false
        }
    }

    private fun loadPrice(mPackage: Package): String {
        val priceDouble = mPackage.product.price.amountMicros.toDouble() / 1000000
        val priceRounded = "%.2f".format(priceDouble)

        return "$priceRounded ${mPackage.product.price.currencyCode}"
    }

    private fun loadNumberTitle(mPackage: Package): String {
        return context.resources.getString(
            when (mPackage.product.id) {
                Constants.SUB_MONTHLY, Constants.SUB_MONTHLY_TRIAL -> R.string.sub_monthly_number
                Constants.SUB_YEARLY, Constants.SUB_YEARLY_TRIAL -> R.string.sub_yearly_number
                else -> R.string.unknown
            }
        )
    }

    private fun loadTitle(mPackage: Package): String {
        return context.resources.getString(
            when (mPackage.product.id) {
                Constants.SUB_MONTHLY -> R.string.sub_montly_title
                Constants.SUB_MONTHLY_TRIAL, Constants.SUB_YEARLY_TRIAL -> R.string.sub_trial_title
                Constants.SUB_YEARLY -> R.string.sub_year_title
                else -> R.string.unknown
            }
        )
    }

    private fun loadShortDescription(mPackage: Package): String {
        val str = context.resources.getString(
            when (mPackage.product.id) {
                Constants.SUB_MONTHLY -> R.string.sub_montly_description
                Constants.SUB_MONTHLY_TRIAL -> R.string.sub_month_trial_description_short
                Constants.SUB_YEARLY_TRIAL -> R.string.sub_year_trial_description_short
                Constants.SUB_YEARLY -> R.string.sub_year_description
                else -> R.string.unknown
            }
        )
        val priceDouble = mPackage.product.price.amountMicros.toDouble() / 1000000
        val priceRounded = "%.2f".format(priceDouble)

        return String.format(str, "$priceRounded ${mPackage.product.price.currencyCode}")
    }

    private fun loadDescription(mPackage: Package): String {
        val str = context.resources.getString(
            when (mPackage.product.id) {
                Constants.SUB_MONTHLY -> R.string.sub_montly_description
                Constants.SUB_MONTHLY_TRIAL -> R.string.sub_month_trial_description
                Constants.SUB_YEARLY_TRIAL -> R.string.sub_year_trial_description
                Constants.SUB_YEARLY -> R.string.sub_year_description
                else -> R.string.unknown
            }
        )
        val priceDouble = mPackage.product.price.amountMicros.toDouble() / 1000000
        val priceRounded = "%.2f".format(priceDouble)

        return String.format(str, "$priceRounded ${mPackage.product.price.currencyCode}")
    }


}