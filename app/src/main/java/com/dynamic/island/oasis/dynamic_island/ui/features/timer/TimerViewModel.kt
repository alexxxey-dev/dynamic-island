package com.dynamic.island.oasis.dynamic_island.ui.features.timer

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.os.Vibrator
import androidx.lifecycle.MutableLiveData
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.OverlayViewModel
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.util.ext.getAppIntent
import com.dynamic.island.oasis.util.ext.safeStartActivity
import com.dynamic.island.oasis.util.ext.doVibration
import com.google.gson.Gson
import kotlinx.coroutines.launch


class TimerViewModel(
    private val vibrator: Vibrator,
    private val context: Context,
    private val prefs: PrefsUtil,
    val di: DiViewModel
) : OverlayViewModel(di) {
    private var timerPackage: String? = null
    private var isCountDown: Boolean? = null

    val time = MutableLiveData<String>()
    val notifActions = MutableLiveData<List<Notification.Action>>()
    val timerActive = MutableLiveData<Boolean>()


    private fun showTimerState(timerState: DiState.Timer) {
        val currentState = di.state.value
        val showBubble = currentState is DiState.ActiveCall || currentState is DiState.IncomingCall || currentState is DiState.Notification || currentState is DiState.Music
        if (showBubble) {
            di.showBubble(timerState)
        } else if (currentState is DiState.Alert || currentState is DiState.QuickAction) {
            di.setPrevState(timerState)
        } else {
            di.setState(timerState)
        }


    }


    private fun hideTimerState() {
        val timerDisplayed = di.state.value is DiState.Timer
        if (timerDisplayed) {
            di.setState(DiState.Main())
        }
        di.hideBubble()
    }

    private fun timerState(packageName:String, intent: Intent): DiState.Timer? {
        val time = intent.getStringExtra(Constants.PARAM_TIME) ?: return null
        val isCountDown = intent.getBooleanExtra(Constants.PARAM_COUNT_DOWN, false)


        this.timerPackage = packageName
        this.time.value = time

        val progress = progress(time, isCountDown)


        Logs.log("onTimerStarted; progress=$progress;")
        return DiState.Timer(
            packageName = packageName,
            expanded = false,
            progress = progress
        )
    }
    fun onTimerStarted(intent: Intent)=viewModelScope.launch {
        val packageName = intent.getStringExtra(Constants.PARAM_PACKAGE) ?: return@launch
        if(!prefs.appCompatible(packageName)) return@launch

        val timerState = timerState(packageName,intent) ?: return@launch
        showTimerState(timerState)
    }

    fun onTimerUpdated(intent: Intent) =viewModelScope.launch{
        val packageName = intent.getStringExtra(Constants.PARAM_PACKAGE) ?: return@launch
        if(!prefs.appCompatible(packageName)) return@launch

        val time = intent.getStringExtra(Constants.PARAM_TIME)
        if (time != null) this@TimerViewModel.time.value = time

        val active = time != null
        if (active != timerActive.value) timerActive.value = active
    }

    fun onTimerStopped(intent: Intent)=viewModelScope.launch {
        Logs.log("onTimerStopped")
        timerPackage = null
        isCountDown = null
        timerActive.value = false
        notifActions.value = emptyList()
        time.value = ""
        hideTimerState()

    }




    fun updateNotifActions(intent: Intent)=viewModelScope.launch {
        try {
            val parcelableList =
                intent.getParcelableArrayExtra(Constants.PARAM_NOTIFCITAION_ACTIONS) as Array<Parcelable>
            val actions = parcelableList.map { it as Notification.Action }
            notifActions.value = actions
        } catch (ex: Exception) {
            Logs.log("updateNotifActions exception")
            ex.printStackTrace()
        }

    }


    fun executeNotifAction(action: Notification.Action)=viewModelScope.launch {
        try {
            action.actionIntent.send()
        } catch (ex: Exception) {
            Logs.log("executeNotifAction exception")
            Logs.exception(ex)
        }

    }

    fun collapseTimer(progress: Float?) =viewModelScope.launch{
        if (timerPackage == null) return@launch
        di.setState(
            DiState.Timer(timerPackage!!, false, progress ?: 0f)
        )
    }

    fun onLongClicked(progress: Float?): Boolean {
        if (timerPackage == null) return false

        vibrator.doVibration(Constants.LONG_CLICK_VIBRATION)
        di.setState(DiState.Timer(timerPackage!!, true, progress ?: 0f))

        return true
    }

    fun openTimerApp() =viewModelScope.launch{
        if(!prefs.settingEnabled(Constants.SET_CLICK_TO_OPEN)) return@launch
        if (timerPackage == null) return@launch
        Logs.log("openTimerApp")
        val intent = context.packageManager.getAppIntent(timerPackage!!) ?: return@launch
        context.safeStartActivity(intent)
    }




    private fun progress(time: String, isCountDown: Boolean): Float =try{
        val seconds = time.split(":").lastOrNull()?.toString()?.toInt() ?: 0
        val maxProgress = 1f
        val maxSeconds = 60f
         if (isCountDown) {
            ((maxSeconds - seconds) / maxSeconds * maxProgress)
        } else {
            (seconds / maxSeconds * maxProgress)
        }
    }catch (ex:Exception){
        ex.printStackTrace()
        0f
    }


}