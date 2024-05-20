package com.dynamic.island.oasis.dynamic_island.util

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController



 fun Service.createWakeLock() =try{
    val tag = if (Build.MANUFACTURER.lowercase() == "huawei") {
        "LocationManagerService"
    } else{
        packageName
    }

    val manager = getSystemService(Context.POWER_SERVICE) as PowerManager
    val wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag)
    wakeLock.acquire()
}catch (ex:Exception){
    ex.printStackTrace()
}


fun Context.destroyReceiver(receiver: BroadcastReceiver?){
    try {
        unregisterReceiver(receiver)
    }catch (ex:Exception){
        ex.printStackTrace()
    }
}
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.createReceiver(receiver: BroadcastReceiver?, filter: IntentFilter): Intent?{
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        }else {
            registerReceiver(receiver, filter)
        }
    }catch (ex:Exception){
        ex.printStackTrace()
        null
    }

}




