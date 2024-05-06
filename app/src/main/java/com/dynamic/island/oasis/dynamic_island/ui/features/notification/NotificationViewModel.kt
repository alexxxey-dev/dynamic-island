package com.dynamic.island.oasis.dynamic_island.ui.features.notification

import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.data.MyNotification
import com.dynamic.island.oasis.dynamic_island.ui.OverlayViewModel
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.getAppIntent
import com.dynamic.island.oasis.util.ext.safeStartActivity
import com.dynamic.island.oasis.util.ext.doVibration
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotificationViewModel(
    private val vibrator: Vibrator,
    private val context: Context,
    val di: DiViewModel,
    private val prefs: PrefsUtil,
    private val gson: Gson,
) : OverlayViewModel(di) {
    val notificationList = MutableLiveData<List<MyNotification>>()
    val actions = HashMap<Int, List<Notification.Action>>()
    val actionsVisible = MutableLiveData<Boolean>()
    val collapsedNotification = MutableLiveData<MyNotification?>()
    val position = MutableLiveData<Int>(0)
    val setPosition = SingleLiveEvent<Int>()


    fun onNotificationAdded(intent: Intent)=viewModelScope.launch {
        val notifications = ArrayList(notificationList.value ?: emptyList())

        saveNotificationPos({
            addNotifList(intent, notifications)
            trimToLimit(notifications)
            notificationList.value = notifications
        },notifications)

        collapsedNotification.value = notifications.firstOrNull()
        showNotifState(notifications.firstOrNull())
        updateActionsVisibility()

    }


    private fun addNotifList(intent:Intent, notifications:ArrayList<MyNotification>){
        gson.fromJson<List<MyNotification>>(
            intent.getStringExtra(Constants.PARAM_NOTIFICATION_LIST),
            object : TypeToken<List<MyNotification>>() {}.type
        ).forEach { mNotification ->
            if (!prefs.appCompatible(mNotification.packageName)) return@forEach
            Logs.log("onNotificationAdded; id=${mNotification.id}; title=${mNotification.title}; text=${mNotification.text}")
            addNotif(mNotification, notifications)
            updateActions(intent, mNotification)
        }
        notifications.sortByDescending { it.sentTimestamp }
    }

    private fun addNotif(notification: MyNotification, notifications: ArrayList<MyNotification>) {
        val index =
            notifications.indexOfFirst { it.equalTo(notification) }

        if (index >= 0) {
            notifications[index] = notification
        } else {
            notifications.add(0, notification)
        }
    }

    private fun trimToLimit(notifications: ArrayList<MyNotification>) {
        val autoClear = prefs.settingEnabled(Constants.SET_CLEAR_NOTIF)
        if (!autoClear) return
        if (notifications.size <= Constants.MAX_NOTIFICATIONS) return

        val startIndex = Constants.MAX_NOTIFICATIONS
        notifications.subList(startIndex, notifications.size).clear()
    }


    fun onSystemNotificationRemove(intent: Intent)=viewModelScope.launch  {
        val notifications = ArrayList(notificationList.value ?: emptyList())
        val id = intent.getStringExtra(Constants.NOTIFICATION_ID)?.toIntOrNull() ?: return@launch
        notifications.removeAll { it.id == id }
        notificationList.value = notifications
        onNotificationRemoved(notifications)
    }

    fun onUserNotificationRemove(notification: MyNotification)=viewModelScope.launch  {
        val notifications = ArrayList(notificationList.value ?: emptyList())
        val index =
            notifications.indexOfFirst { it.equalTo(notification) }
        if (index < 0) return@launch
        notifications.removeAt(index)
        notificationList.value = notifications
        onNotificationRemoved(notifications)
    }

    private fun onNotificationRemoved(notifications: List<MyNotification>) {
        collapsedNotification.value = notifications.firstOrNull()
        if (notifications.isEmpty()) hideNotifState()
    }

    private fun saveNotificationPos(action:()->Unit, notifications: ArrayList<MyNotification>){
        val pos = position.value ?: 0
        val current = if (pos < notifications.size) notifications[pos] else null

        action()

        val index =
            notifications.indexOfFirst { it.equalTo(current) }
        setPosition.value = if (index >= 0) index else 0
    }


    private fun showNotifState(mNotification: MyNotification?) {
        if (mNotification == null) return

        val currentState = di.state.value

        if (currentState is DiState.Notification && currentState.expanded) return

        val notificationState =
            DiState.Notification(expanded = false, packageName = mNotification.packageName)

        val showBubble =
            currentState is DiState.ActiveCall || currentState is DiState.IncomingCall || currentState is DiState.Music || currentState is DiState.Timer
        if (showBubble) {
            di.showBubble(notificationState)
        } else if (currentState is DiState.Alert || currentState is DiState.QuickAction) {
            di.setPrevState(notificationState)
        } else {
            di.setState(notificationState)
        }
    }


    private fun hideNotifState() {
        if (di.state.value is DiState.Notification) {
            di.setState(DiState.Main())
        }
        di.hideBubble()
    }

    fun updateActionsVisibility() {
        val enabled = prefs.settingEnabled(Constants.SET_NOTIFICATION_ACTIONS)
        actionsVisible.value = enabled
    }

    private fun updateActions(intent: Intent, mNotification: MyNotification) {
        val mActions = intent.getParcelableArrayExtra(Constants.PARAM_NOTIFCITAION_ACTIONS)
        if (mActions != null) {
            actions[mNotification.id] =
                mActions.map { it as Notification.Action }.toList()
        }
    }

    fun replyNotification(view: View, action: Notification.Action?, text: String)=viewModelScope.launch  {
        if (action == null || action.remoteInputs.isEmpty()) return@launch
        view.context.analyticsEvent("on_reply_notification_clicked")
        try {
            val intent = Intent().addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            RemoteInput.addResultsToIntent(action.remoteInputs, intent, Bundle().apply {
                putString(action.remoteInputs.first().resultKey, text)
            })
            action.actionIntent.send(context, 0, intent)
        } catch (ex: Exception) {
            Logs.log("reply notification exception")
            Logs.exception(ex)
        }
    }

    fun executeNotifAction(action: Notification.Action): Boolean {
        if (action.remoteInputs != null && action.remoteInputs.isNotEmpty()) {
            return true
        }


        try {
            action.actionIntent.send()
        } catch (ex: Exception) {
            Logs.log("executeNotifAction exception")
            Logs.exception(ex)
        }

        return false
    }

    fun onExpandNotification() :Boolean {
        try {
            val notifications = ArrayList(notificationList.value ?: emptyList())
            if (notifications.isNotEmpty()) setPosition.value = 0 else return false

            vibrator.doVibration(Constants.LONG_CLICK_VIBRATION)
            di.setState(
                DiState.Notification(
                    expanded = true,
                    packageName = collapsedNotification.value!!.packageName
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
        return true
    }

    fun onCollapseNotification(): Boolean {
        di.setState(
            DiState.Notification(
                expanded = false,
                packageName = collapsedNotification.value!!.packageName
            )
        )
        return true
    }


    fun openApp(notification: MyNotification?)=viewModelScope.launch  {
        if (!prefs.settingEnabled(Constants.SET_CLICK_TO_OPEN)) return@launch
        if (notification == null) return@launch
        try {
            val state = di.state.value ?: return@launch
            val packageName = notification.packageName
            val intent = context.packageManager.getAppIntent(packageName)
            context.safeStartActivity(intent)
            if (state is DiState.Notification && state.expanded) {
                di.state.value = state.copy(expanded = false)
            }
        } catch (ex: Exception) {
            Logs.log("openApp exception")
            Logs.exception(ex)
        }
    }
}