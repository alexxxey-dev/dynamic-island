package com.dynamic.island.oasis.dynamic_island.ui.features.quick_action

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.dynamic_island.ui.OverlayViewModel
import com.dynamic.island.oasis.dynamic_island.util.Flashlight
import kotlinx.coroutines.*


class QuickActionViewModel(
    private val prefs: PrefsUtil,
    private val flashlight: Flashlight,
    private val showIntent: (intent: Intent) -> Unit,
    private val performAction: (action: Int) -> Unit,
    val di: DiViewModel
) : OverlayViewModel(di) {

    val flashlightVisible = MutableLiveData<Boolean>()
    val screenshotVisible = MutableLiveData<Boolean>()

    init {
        flashlightVisible.value = flashlight.hasFlashlight()
        screenshotVisible.value = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun openCamera()=viewModelScope.launch {
        showIntent(Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        hideView()
    }


    fun lockScreen()=viewModelScope.launch {
        performAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
        hideView()
    }


    fun toggleFlashlight()=viewModelScope.launch {
        flashlight.toggleFlashlight()
    }



    fun takeScreenshot(mContext: Context) =viewModelScope.launch {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return@launch
        di.visible.value = false
        if (prefs.settingEnabled(Constants.SET_SCREENSHOT_APP))   di.showAppScreenshot()
        delay(500)
        performAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)

        mContext.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler()) {
                override fun onChange(selfChange: Boolean) {
                    mContext.contentResolver.unregisterContentObserver(this)
                    if (prefs.settingEnabled(Constants.SET_SCREENSHOT_APP)) di.hideAppScreenshot()
                    di.visible.value = true

                }
            }
        )

    }


    fun openSettings()  =viewModelScope.launch{
        showIntent(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        hideView()
    }


    fun hideView() =viewModelScope.launch {
        val previous = di.previousState.value
        val current = di.state.value
        if (!(current is DiState.QuickAction)) return@launch

        if (previous != null && !(previous is DiState.QuickAction) && !(previous is DiState.Alert)) {
            di.setState(previous)
        } else {
            di.setState(DiState.Main())
        }
    }



}