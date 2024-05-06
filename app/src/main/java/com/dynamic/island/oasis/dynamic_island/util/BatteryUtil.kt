package com.dynamic.island.oasis.dynamic_island.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.dynamic.island.oasis.util.ext.createReceiver

class BatteryUtil(private val context:Context) {

    fun batteryLevel():Int{
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.createReceiver(null, ifilter)
        }
        return batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale
        } ?: 0
    }

}