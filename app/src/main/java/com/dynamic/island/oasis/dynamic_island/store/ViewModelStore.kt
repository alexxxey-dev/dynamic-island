package com.dynamic.island.oasis.dynamic_island.store

import android.app.KeyguardManager
import android.app.NotificationManager
import android.os.Vibrator
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.util.DiParamsProvider
import com.dynamic.island.oasis.dynamic_island.service.MainService
import com.dynamic.island.oasis.dynamic_island.ui.features.alert.AlertViewModel
import com.dynamic.island.oasis.dynamic_island.ui.features.call.CallViewModel
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.dynamic_island.ui.bubble.BubbleViewModel
import com.dynamic.island.oasis.dynamic_island.ui.main.MainViewModel
import com.dynamic.island.oasis.dynamic_island.ui.features.music.MusicViewModel
import com.dynamic.island.oasis.dynamic_island.ui.features.notification.NotificationViewModel
import com.dynamic.island.oasis.dynamic_island.ui.features.quick_action.QuickActionViewModel
import com.dynamic.island.oasis.dynamic_island.ui.features.timer.TimerViewModel
import com.dynamic.island.oasis.dynamic_island.util.BatteryUtil
import com.dynamic.island.oasis.dynamic_island.util.Flashlight
import com.dynamic.island.oasis.dynamic_island.util.PhoneUtil
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.util.ext.safeStartActivity
import com.google.gson.Gson

class ViewModelStore(
    private val acsb: MainService,
    private val prefs: PrefsUtil,
    private val permissions: PermissionsUtil,
    private val diParamsProvider: DiParamsProvider,
    private val battery: BatteryUtil,
    private val gson:Gson,
    private val vibrator: Vibrator,
    private val notifications:NotificationManager,
    private val phone:PhoneUtil,
    private val flashlight: Flashlight,
    private val keyguard:KeyguardManager
) {




    val diViewModel = DiViewModel(acsb, prefs, diParamsProvider,keyguard)

    val bubbleViewModel = BubbleViewModel(diViewModel,phone,acsb, prefs)
    val alertViewModel = AlertViewModel(
        prefs,
        battery,
        acsb.resources,
        diViewModel
    )
    val callViewModel = CallViewModel(
        vibrator,
        acsb,
        prefs,
        phone,
        diViewModel
    )
    val mainViewModel = MainViewModel(
        acsb,
        prefs,
        vibrator,
        diViewModel,
    )
    val musicViewModel = MusicViewModel(
        diViewModel,
        phone,
        vibrator,
        prefs,
        acsb.resources,
        acsb.packageManager,
        showIntent = { acsb.safeStartActivity(it) }
    )
    val notificationViewModel = NotificationViewModel(
        vibrator,acsb,diViewModel,prefs,gson
    )
    val quickActionViewModel = QuickActionViewModel(
        flashlight,
        showIntent = { acsb.safeStartActivity(it) },
        diViewModel
    )
    val timerViewModel = TimerViewModel(
        vibrator,
        acsb,
        prefs,
        diViewModel
    )

    private val overlayViewModels = listOf(
        alertViewModel,
        callViewModel,
        mainViewModel,
        musicViewModel,
        notificationViewModel,
        quickActionViewModel,
        timerViewModel,
        bubbleViewModel
    )






    fun updateBackground() {
        diViewModel.updateBackground()
    }


    fun onDestroy() {
        overlayViewModels.forEach { it.onDestroy() }
    }
}