package com.dynamic.island.oasis.data

import android.content.Context
import android.content.Intent
import com.dynamic.island.oasis.Constants
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.Job
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MyConfig(private val context: Context) {
    var fetched: Boolean = false
        private set

    init {
        Firebase.remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        })
        Firebase.remoteConfig.setDefaultsAsync(
            mapOf(
                Constants.CONFIG_PARAM_OFFERING to "7dyearly_montly",
                Constants.CONFIG_PARAM_PAYWALL to Constants.PAYWALL_A
            )
        )
        fetch()
    }


    private fun fetch() {
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener {
            fetched = true
            context.sendBroadcast(Intent(Constants.ACTION_CONFIG_FETCH))
        }
    }

    fun loadOfferingVersion(): String {
        return "7dyearly_montly"
        //return Firebase.remoteConfig.getString(Constants.CONFIG_PARAM_OFFERING)
    }


    fun loadPaywallVersion(): String {
        return Constants.PAYWALL_B
        //return Firebase.remoteConfig.getString(Constants.CONFIG_PARAM_PAYWALL)
    }
}