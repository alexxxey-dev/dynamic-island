package com.dynamic.island.oasis.dynamic_island.listeners.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.UserHandle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.service.MainService
import com.dynamic.island.oasis.dynamic_island.service.ServiceWrapper
import com.dynamic.island.oasis.dynamic_island.util.PhoneUtil
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class NotificationListener : NotificationListenerService() {
    private var sender: NotificationSender? = null
    private var timer: TimerListener? = null
    private var phone:PhoneUtil? = null
    private var job: Job? = null
    private var gson: Gson? = null
    private var telecom:TelecomManager? = null
    override fun onDestroy() {
        sendBroadcast(Intent(Constants.DESTROY_MEDIA_LISTENER))
        super.onDestroy()
    }
    override fun onListenerDisconnected() {
        sender = null
        timer?.onDestroy()
        timer = null
        phone = null
        gson = null
        telecom = null
        job?.cancel()
        job = null
        sendBroadcast(Intent(Constants.DESTROY_MEDIA_LISTENER))
        Logs.log("notificationListenerDisconnected")
        requestRebind(ComponentName(this, NotificationListener::class.java))
        super.onListenerDisconnected()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Logs.log("notificationListenerConnected")
        gson = Gson()
        telecom = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        phone = PhoneUtil(telecom!!,contentResolver,resources  )
        sender = NotificationSender(this, gson!!)
        timer = TimerListener(this)
        sendBroadcast(Intent(Constants.CREATE_MEDIA_LISTENER))
    }

    override fun onBind(intent: Intent?): IBinder? {
        sendBroadcast(Intent(Constants.CREATE_MEDIA_LISTENER))
        return super.onBind(intent)
    }



    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        sendBroadcast(Intent(Constants.CREATE_MEDIA_LISTENER))
        MainService.startViaWorker(this)
        if(job?.isActive == true) job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            val isTimer = timer?.onNotificationPosted(sbn) ?: false
            Logs.log("onNotificationPosted; isTimer=$isTimer")

            if (!isTimer && notificationValid(sbn)) sender?.onAdded(sbn)
            sendCallData(sbn)
        }

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        MainService.startViaWorker(this)
        val isTimer = timer?.onNotificationRemoved(sbn) ?: false
        if(!isTimer && notificationValid(sbn)) sender?.onRemoved(sbn)
    }


    private fun notificationValid(sbn: StatusBarNotification): Boolean {
        return try {
            val notification = sbn.notification
            if (isCall(sbn)) throw Exception()

            when (notification.category) {
                Notification.CATEGORY_SYSTEM,
                Notification.CATEGORY_TRANSPORT,
                Notification.CATEGORY_SERVICE,
                Notification.CATEGORY_CALL -> false
                else -> true
            }
        } catch (ex: Exception) {
            false
        }

    }

    private fun sendCallData(sbn: StatusBarNotification) {
        //Logs.log("notification = ${sbn.notification}")
        val notification = sbn.notification ?: return

        if (!isCall(sbn)) return
        val phone = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val intent = Intent(Constants.ACTION_UPDATE_CALL_DATA).apply {
            putExtra(Constants.PARAM_NOTIFCITAION_ACTIONS, notification.actions)
            putExtra(Constants.PARAM_PHONE_TITLE, phone)
        }
        sendBroadcast(intent)
    }


    private fun isCall(sbn: StatusBarNotification): Boolean {
        //Logs.log("notification = ${sbn.notification}; category = ${sbn.notification.category}; package = ${sbn.packageName}")
        return sbn.notification.category == Notification.CATEGORY_CALL || sbn.packageName == phone?.defaultCallApp()
    }

}