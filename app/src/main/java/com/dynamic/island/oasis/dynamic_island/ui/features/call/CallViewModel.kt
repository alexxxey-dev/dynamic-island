package com.dynamic.island.oasis.dynamic_island.ui.features.call

import android.app.Notification
import android.app.Notification.Action
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.os.Vibrator
import androidx.lifecycle.MutableLiveData
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.data.MyContact
import com.dynamic.island.oasis.dynamic_island.listeners.CallTimeListener
import com.dynamic.island.oasis.dynamic_island.ui.OverlayViewModel
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.dynamic_island.util.PhoneUtil
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.util.ext.getAppIntent
import com.dynamic.island.oasis.util.ext.safeStartActivity
import com.dynamic.island.oasis.util.ext.doVibration
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class CallViewModel(
    private val vibrator: Vibrator,
    private val context: Context,
    private val prefs: PrefsUtil,
    private val phone: PhoneUtil,
    val di: DiViewModel
) : OverlayViewModel(di) {
    val contact = MutableLiveData<MyContact?>()
    val callTime = MutableLiveData<String>()



    val notifActions = MutableLiveData<List<Action>?>()

    private val listener = object : CallTimeListener() {
        override fun onTimeUpdated(time: String) {
            callTime.value = time
        }
    }



    override fun onDestroy() {
        listener.stopListening()
    }

    private suspend fun showRingingState() {
        di.setState(DiState.IncomingCall())
    }



    private suspend fun showActiveState() {
        val callState = DiState.ActiveCall(expanded = false)
        val prev = di.previousState.value

        if (prev is DiState.Timer || prev is DiState.Notification || prev is DiState.Music) {
            di.setState(prev)
            di.showBubble(callState)
        } else {
            di.setState(callState)
        }
    }


    private suspend fun hideActiveState() {
        val callDisplayed =
            di.state.value is DiState.ActiveCall || di.state.value is DiState.IncomingCall
        if (callDisplayed) {
            di.setState(DiState.Main())
        }

        di.hideBubble()
    }

    //adb shell am start -a android.intent.action.CALL -d tel:+79175878779
    fun onCallRinging() =viewModelScope.launch{
        Logs.log("onCallRinging;")
        if (!prefs.appCompatible(phone.defaultCallApp()) || !prefs.appCompatible(phone.callSessionPackage())) return@launch

        loadContact()
        showRingingState()
    }

    fun onCallActive()=viewModelScope.launch {
        Logs.log("onCallActive;")
        if (!prefs.appCompatible(phone.defaultCallApp())) return@launch

        listener.startListening()
        showActiveState()
    }

    fun onCallFinished()=viewModelScope.launch {
        Logs.log("onCallFinished;")
        if (!prefs.appCompatible(phone.defaultCallApp())) return@launch

        listener.stopListening()
        notifActions.value = null
        contact.value = null
        hideActiveState()
    }

    fun updateCallData(intent: Intent) {
        updateNotifActions(intent)
        loadContact(intent, true)
    }


    private fun updateNotifActions(intent: Intent) {
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

    fun openCallApp() =viewModelScope.launch{
        if (!prefs.settingEnabled(Constants.SET_CLICK_TO_OPEN)) return@launch
        val pn = phone.defaultCallApp()
        val intent = context.packageManager.getAppIntent(pn) ?: return@launch
        context.safeStartActivity(intent)
    }

    fun collapseCall() =viewModelScope.launch{
        di.setState(DiState.ActiveCall(expanded = false))
    }

    fun expandCall(): Boolean {
        vibrator.doVibration(Constants.LONG_CLICK_VIBRATION)
        di.setState(DiState.ActiveCall(expanded = true))
        return true
    }

    fun executeNotifAction(action: Action)=viewModelScope.launch {
        try {
            action.actionIntent.send()
        } catch (ex: Exception) {
            Logs.log("executeNotifAction exception")
            Logs.exception(ex)
        }
    }

    private fun loadContact(intent: Intent? = null, force: Boolean = false) {
        CoroutineScope(Dispatchers.Main).launch {
            if (contact.value != null && !force) return@launch
            val title = intent?.getStringExtra(Constants.PARAM_PHONE_TITLE)
            Logs.log("loadContact; title = $title")
            val mCall = phone.getCallContact(title)
            contact.value = mCall
        }
    }


}