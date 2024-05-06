package com.dynamic.island.oasis.dynamic_island.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.MutableLiveData
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.util.DiParamsProvider
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.data.DiBackground
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.util.PhoneUtil
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.AppData
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.getAccentColor
import com.dynamic.island.oasis.util.ext.getAppLogo
import com.dynamic.island.oasis.util.ext.getAppTitle
import com.dynamic.island.oasis.util.ext.isActivity

class DiViewModel(
    private val context: Context,
    private val packageManager: PackageManager,
    private val prefs: PrefsUtil,
    private val diParamsProvider: DiParamsProvider,
    private val keyguard: KeyguardManager
) {
    val background = MutableLiveData<DiBackground>()

    val bubbleState = MutableLiveData<DiState>()
    val state = MutableLiveData<DiState>(DiState.Main(animShake = true))
    val previousState = MutableLiveData<DiState>()

    val params = MutableLiveData<DiParams>()
    val visible = MutableLiveData<Boolean>(true)
    val screenLocked = MutableLiveData<Boolean>(false)


    val activePackageName = MutableLiveData<String>()


    val showBubble = SingleLiveEvent<DiState>()
    val hideBubble = SingleLiveEvent<Unit>()
    val showAppScreenshot = SingleLiveEvent<AppData>()
    val hideAppScreenshot = SingleLiveEvent<Unit>()

    init {
        updateDiParams()
        updateBackground()
    }


    fun onScreenOff() {
        val showOnLockscreen = prefs.settingEnabled(Constants.SET_LOCK_SCREEN)

        Logs.log("onScreenLocked;")

        if (!showOnLockscreen) visible.value = false
        screenLocked.value = true
    }

    fun onScreenAction() {
        val showOnLockscreen = prefs.settingEnabled(Constants.SET_LOCK_SCREEN)


        if (!keyguard.isKeyguardLocked) {
            Logs.log("onScreenUnlocked;")
            if (!showOnLockscreen) visible.value = true
            screenLocked.value = false
        }
    }

    fun setPrevState(state: DiState?) {
        if (state == null) return
        previousState.value = when (state) {
            is DiState.QuickAction, is DiState.IncomingCall, is DiState.Alert -> state
            is DiState.Main -> state.copy(animShake = false)
            is DiState.Music -> state.copy(expanded = false)
            is DiState.ActiveCall -> state.copy(expanded = false)
            is DiState.Timer -> state.copy(expanded = false)
            is DiState.Notification -> state.copy(expanded = false)
        }
    }

    fun setState(state: DiState) {
        setPrevState(this.state.value)
        if (bubbleState.value != null && state is DiState.Main) {
            this.state.value = bubbleState.value
        } else {
            this.state.value = state
        }


    }

    fun onConfigurationChange(intent: Intent) {
        val disableLandscape = prefs.settingEnabled(Constants.SET_DISABLE_LANDSCAPE)
        val config = context.resources.configuration
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && disableLandscape) {
            visible.value = false
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            visible.value = true
        }
    }



    fun updateDiParams() {
        params.value = diParamsProvider.provide()
    }

    fun showAppScreenshot() {
        val mPackage = activePackageName.value ?: return
        val mTitle = context.getAppTitle(mPackage)
        val mLogo = context.packageManager.getAppLogo(mPackage)
        val mColor = context.getAccentColor(mPackage)
        if (mTitle.isNullOrBlank() || mLogo == null || mColor == null) return
        val mData = AppData(
            logo = mLogo,
            title = mTitle,
            color = Color.BLACK,
            diBackground = loadBackground()
        )

        showAppScreenshot.value = mData
    }

    fun hideAppScreenshot() {
        hideAppScreenshot.value = Unit
    }

    fun showBubble(state: DiState) {
        showBubble.value = state
    }

    fun hideBubble() {
        hideBubble.value = Unit
    }

    fun provideParams() = diParamsProvider.provide()

    private fun loadBackground():DiBackground{
        val color = prefs.backgroundColor()
        val resource =
            if (prefs.isBackgroundNotch()) R.drawable.shape_di_notch else R.drawable.shape_di_rounded
        return DiBackground(resource, color)
    }
    fun updateBackground() {


        background.value = loadBackground()
    }


    fun loadPackageName(event: AccessibilityEvent): String? {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return null
        if (event.packageName == null || event.className == null) return null
        if (!packageManager.isActivity(event)) return null

        return event.packageName.toString()
    }



}