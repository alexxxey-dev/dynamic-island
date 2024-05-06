package com.dynamic.island.oasis.dynamic_island.listeners.notifications

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.MyNotification
import com.dynamic.island.oasis.util.ext.getAppTitle
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import java.util.*


class NotificationSender(
    private val listener: NotificationListener,
    private val gson:Gson
) {



    fun onRemoved(sbn: StatusBarNotification) {
        listener.sendBroadcast(
            Intent(Constants.ACTION_REMOVED_NOTIFICATION).putExtra(
                Constants.NOTIFICATION_ID,
                sbn.id.toString()
            )
        )
    }


    suspend fun onAdded(sbn: StatusBarNotification) = withContext(Dispatchers.IO){
        val actions = sbn.notification.actions ?: emptyArray()
        val notifications = parse(sbn, sbn.notification)
        send(actions, notifications)
    }

    private fun send(actions:Array<Notification.Action>, notifications: List<MyNotification>) {
        listener.sendBroadcast(Intent(Constants.ACTION_NEW_NOTIFICATION).apply {
            putExtra(Constants.PARAM_NOTIFICATION_LIST, gson.toJson(notifications))
            putExtra(Constants.PARAM_NOTIFCITAION_ACTIONS, actions)
        })
    }

    private fun parse(
        sbn: StatusBarNotification,
        notification: Notification?
    ): List<MyNotification> {
        val extras = notification?.extras ?: throw Exception("empty notification extras")
        val title = extras.get(Notification.EXTRA_TITLE)?.toString()
        val count = extras.getParcelableArray(Notification.EXTRA_MESSAGES)?.size ?: 1
        val messages: Array<out Parcelable>? =
            extras.getParcelableArray(Notification.EXTRA_MESSAGES)

        if (messages.isNullOrEmpty()) {
            val mText = parseText(extras)
            val mTitle = title ?: listener.getAppTitle(sbn.packageName)
            if (mText.isNullOrBlank() || mTitle.isNullOrBlank()) return emptyList()
            return listOf(
                MyNotification(
                    title = mTitle,
                    text = mText,
                    packageName = sbn.packageName,
                    id = sbn.id,
                    sentTimestamp = notification.`when`,
                    postTimestamp = sbn.postTime,
                    count = count
                )
            )
        }

        val result = ArrayList<MyNotification>()
        messages.forEach {
            val mBundle = it as Bundle
            val mText = mBundle.get("text")?.toString()
            val mSender = mBundle.get("sender")?.toString()
            val mTime = mBundle.getLong("time")
            val mTitle = title ?: mSender ?: listener.getAppTitle(sbn.packageName)

            if (!mTitle.isNullOrBlank() && !mText.isNullOrBlank()) {
                val notif = MyNotification(
                    title = mTitle,
                    text = mText,
                    packageName = sbn.packageName,
                    id = sbn.id,
                    sentTimestamp = mTime,
                    count = count,
                    postTimestamp = sbn.postTime
                )
                result.add(notif)
            }
        }

        return result
    }


    private fun parseText(extras: Bundle): String? = try {
        val lines = StringBuilder()
        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach {
            val str = it.toString()
            if (str.isNotBlank()) lines.append("$str\n")
        }
        val bigText = extras.get(Notification.EXTRA_BIG_TEXT)?.toString()
        val text = extras.get(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.get(Notification.EXTRA_SUB_TEXT)?.toString()
        val infoText = extras.get(Notification.EXTRA_INFO_TEXT)?.toString()
        val summaryText = extras.get(Notification.EXTRA_SUMMARY_TEXT)?.toString()

        if (!bigText.isNullOrBlank()) {
            bigText
        } else if (!text.isNullOrBlank()) {
            text
        } else if (lines.toString().isNotBlank()) {
            lines.toString()
        } else if (!subText.isNullOrBlank()) {
            subText
        } else if (!infoText.isNullOrBlank()) {
            infoText
        } else if (!summaryText.isNullOrBlank()) {
            summaryText
        } else {
            null
        }
    } catch (ex: Exception) {
        Logs.log("can't parse notification text")
        Logs.exception(ex)
        null
    }


}