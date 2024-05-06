package com.dynamic.island.oasis.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo


class NetworkUtil(private val manager: ConnectivityManager) {

    companion object{
        const val TYPE_WIFI = 1
        const val TYPE_MOBILE = 2
        const val TYPE_NOT_CONNECTED = 0
        const val NETWORK_STATUS_NOT_CONNECTED = 0
        const val NETWORK_STATUS_WIFI = 1
        const val NETWORK_STATUS_MOBILE = 2
    }
    val isConnected: Boolean
        get() {
            val activeNetworkInfo = getNetworkInfo()
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    private fun getConnectivityStatus(): Int {
        val activeNetwork = getNetworkInfo()
        if (null != activeNetwork) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) return NetworkUtil.TYPE_WIFI
            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) return NetworkUtil.TYPE_MOBILE
        }
        return NetworkUtil.TYPE_NOT_CONNECTED
    }
    fun getConnectivityStatusString(): Int {
        val conn: Int = getConnectivityStatus()
        var status = 0
        when (conn) {
            NetworkUtil.TYPE_WIFI -> {
                status = NetworkUtil.NETWORK_STATUS_WIFI
            }
            NetworkUtil.TYPE_MOBILE -> {
                status = NetworkUtil.NETWORK_STATUS_MOBILE
            }
            NetworkUtil.TYPE_NOT_CONNECTED -> {
                status = NetworkUtil.NETWORK_STATUS_NOT_CONNECTED
            }
        }
        return status
    }
    private fun getNetworkInfo(): NetworkInfo? {
        return manager.activeNetworkInfo
    }

}