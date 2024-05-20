package com.dynamic.island.oasis.dynamic_island.service

import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.listeners.restart.RestartReceiver
import com.dynamic.island.oasis.dynamic_island.listeners.restart.RestartWorker
import com.dynamic.island.oasis.dynamic_island.util.Notifications
import com.dynamic.island.oasis.dynamic_island.util.createReceiver
import com.dynamic.island.oasis.dynamic_island.util.createWakeLock
import com.dynamic.island.oasis.dynamic_island.util.destroyReceiver
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit


abstract class ServiceWrapper : Service() {
    abstract fun onStart()
    abstract fun onStop()


    override fun onBind(intent: Intent?): IBinder? = null

    private var wakeLock:PowerManager.WakeLock? = null



    override fun onCreate() {
        super.onCreate()
        createWakeLock()
        onStart()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = Notifications(this)
        startForeground(
            Notifications.NOTIFICATION_ID,
            notification.buildServiceNotification("App is running")
        )
        return START_STICKY
    }


    override fun onDestroy() {
        sendBroadcast(Intent(this, RestartReceiver::class.java))

        try {
            wakeLock?.let { if(it.isHeld) it.release() }

        }catch (ex:Exception){
            ex.printStackTrace()
        }
        onStop()
        super.onDestroy()
    }






}