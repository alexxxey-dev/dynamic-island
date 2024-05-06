package com.dynamic.island.oasis

import android.content.Context
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.onesignal.notifications.INotificationReceivedEvent
import com.onesignal.notifications.INotificationServiceExtension


@Keep
class MyNotificationService : INotificationServiceExtension {
    override fun onNotificationReceived(event: INotificationReceivedEvent) {

        val prefs = PrefsUtil(event.context, event.context.getSharedPreferences(event.context.packageName, Context.MODE_PRIVATE))
        if(prefs.settingEnabled(Constants.SET_DISABLE_NOTIFICATIONS)){
            event.preventDefault()
            return
        }


    }
}