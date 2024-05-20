package com.dynamic.island.oasis.dynamic_island.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.ui.main.MainActivity

class Notifications(private val context:Context) {
    companion object{
        const val NOTIFICATION_ID = 123
    }

    private fun createPendingIntent(intent: Intent)=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
    } else {
        PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun buildServiceNotification(  body:String): Notification {
        createNotificationChannel()
        val title = context.getString(R.string.app_name)
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = createPendingIntent(notificationIntent)
        return NotificationCompat.Builder(context, context.packageName)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .build()
    }


   private fun createNotificationChannel() {
       if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
       val title = context.getString(R.string.app_name)
       val serviceChannel = NotificationChannel(
           context.packageName,
           title,
           NotificationManager.IMPORTANCE_DEFAULT
       )
       context.getSystemService(NotificationManager::class.java).createNotificationChannel(serviceChannel)
   }
}