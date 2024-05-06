package com.dynamic.island.oasis.dynamic_island.ui.main

import android.content.Context
import android.content.Intent
import android.os.Vibrator
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.Constants.LONG_CLICK_VIBRATION
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.OverlayViewModel
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.ui.main.MainActivity
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.util.ext.safeStartActivity
import com.dynamic.island.oasis.util.ext.doVibration
import kotlinx.coroutines.launch


class MainViewModel(
    private val context:Context,
    private val prefs: PrefsUtil,
    private val vibrator: Vibrator,
    val di: DiViewModel
) : OverlayViewModel(di) {


    fun onClicked()=viewModelScope.launch{
        if(!prefs.settingEnabled(Constants.SET_CLICK_TO_OPEN)) return@launch
        context.safeStartActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
    }

    fun onLongClicked(): Boolean {
        if(!prefs.settingEnabled(Constants.SET_QUICK_ACTION)) return false
        vibrator.doVibration(LONG_CLICK_VIBRATION)
        di.setState(DiState.QuickAction())
        return true
    }


    fun showLockIcon() = prefs.settingEnabled(Constants.SET_LOCK_SCREEN)



}