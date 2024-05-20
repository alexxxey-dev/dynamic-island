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
    private val flashlight: Flashlight,
    private val showIntent: (intent: Intent) -> Unit,
    val di: DiViewModel
) : OverlayViewModel(di) {

    val flashlightVisible = MutableLiveData<Boolean>()


    init {
        flashlightVisible.value = flashlight.hasFlashlight()
    }

    fun openCamera()=viewModelScope.launch {
        showIntent(Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        hideView()
    }





    fun toggleFlashlight()=viewModelScope.launch {
        flashlight.toggleFlashlight()
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