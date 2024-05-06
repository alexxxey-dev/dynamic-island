package com.dynamic.island.oasis.dynamic_island.ui.bubble

import android.content.Context
import android.content.Intent
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.OverlayViewModel
import com.dynamic.island.oasis.dynamic_island.util.PhoneUtil
import com.dynamic.island.oasis.util.ext.getAppIntent
import com.dynamic.island.oasis.util.ext.safeStartActivity
import kotlinx.coroutines.launch

class BubbleViewModel(
    private val di: DiViewModel,
    private val phone: PhoneUtil,
    private val context: Context,
    private val prefs:PrefsUtil
) : OverlayViewModel(di) {


    fun openApp() =viewModelScope.launch{
        if(!prefs.settingEnabled(Constants.SET_CLICK_TO_OPEN)) return@launch
        val state = di.bubbleState.value ?: return@launch
        val pn = when (state) {
            is DiState.Notification -> state.packageName
            is DiState.Timer -> state.packageName
            is DiState.Music -> state.packageName
            is DiState.ActiveCall, is DiState.IncomingCall -> phone.defaultCallApp()
            else -> null
        } ?: return@launch
        val intent = context.packageManager.getAppIntent(pn) ?: return@launch
        context.safeStartActivity(intent)
    }
}