package com.dynamic.island.oasis.dynamic_island.listeners.notifications

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.RemoteViews
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.Logs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TimerListener(private val listener: NotificationListener) {
    private var remoteViews: RemoteViews? = null
    private var packageName: String? = null
    private var id: Int? = null
    private var context: Context? = null
    private var inflater: LayoutInflater? = null

    private var job: Job? = null
    private fun start() = CoroutineScope(Dispatchers.IO).launch {
        while(this.isActive){
            val chronometer = chronometer(remoteViews, packageName)

            val intent = Intent(Constants.ACTION_UPDATE_TIMER).apply {
                putExtra(Constants.PARAM_TIME, chronometer?.text?.toString())
                putExtra(Constants.PARAM_PACKAGE, packageName)
            }

            listener.sendBroadcast(intent)

            delay(Constants.TIMER_DELAY)
        }
    }



    suspend fun onNotificationPosted(sbn: StatusBarNotification): Boolean = withContext(Dispatchers.IO) {
        val mRemoteViews = remoteViews(sbn.notification)
        val chronometer = chronometer(mRemoteViews, sbn.packageName)

        val newTimer = id == null && chronometer != null && chronometer.text.any { it.isDigit() }
        val updateTimer = id != null && id == sbn.id

        if (newTimer || updateTimer) {
            id = sbn.id
            remoteViews = mRemoteViews
            packageName = sbn.packageName
            val intent = Intent(Constants.ACTION_UPDATE_TIMER_ACTIONS).apply {
                putExtra(Constants.PARAM_NOTIFCITAION_ACTIONS, sbn.notification.actions)
            }
            listener.sendBroadcast(intent)
        }


        if (newTimer) {
            val intent =
                Intent(Constants.ACTION_START_TIMER).apply {
                    putExtra(Constants.PARAM_COUNT_DOWN, chronometer?.isCountDown ?: false)
                    putExtra(Constants.PARAM_PACKAGE, packageName)
                    putExtra(Constants.PARAM_TIME, chronometer?.text?.toString())
                }
            listener.sendBroadcast(intent)
            job = start()
        }

        return@withContext newTimer || updateTimer
    }


    fun onNotificationRemoved(sbn: StatusBarNotification): Boolean {
        if (packageName == null || sbn.packageName != packageName) {
            return false
        }
        id = null
        inflater = null
        context = null
        remoteViews = null
        packageName = null
        listener.sendBroadcast(Intent(Constants.ACTION_STOP_TIMER))
        job?.cancel()
        job = null
        return true
    }


    fun onDestroy(){
        id = null
        inflater = null
        context = null
        remoteViews = null
        packageName = null
    }
    private fun chronometer(
        remoteViews: RemoteViews?,
        packageName: String?
    ): Chronometer? {
        if (remoteViews == null || packageName == null) return null

        return try {
            val context = this.context ?: listener.createPackageContext(
                packageName,
                Context.CONTEXT_RESTRICTED
            )
            val inflater = this.inflater ?: context?.getSystemService(LayoutInflater::class.java)
            val view =
                inflater?.inflate(remoteViews.layoutId, null, false) as ViewGroup
            remoteViews.reapply(context, view)

            RecursiveFinder(Chronometer::class.java).expand(view).firstOrNull()
        } catch (ex: Exception) {
            Logs.log("chronometer exception")
            Logs.exception(ex)
            null
        }
    }


    private fun remoteViews(notification: Notification?): RemoteViews? {
        if (notification == null) return null
        return notification.contentView
            ?: notification.bigContentView
            ?: notification.tickerView
            ?: notification.headsUpContentView
            ?: null
    }

    private class RecursiveFinder<T : View?>(clazz: Class<T>) {
        private val list: ArrayList<T>
        private val clazz: Class<T>

        init {
            list = ArrayList()
            this.clazz = clazz
        }

        fun expand(viewGroup: ViewGroup): ArrayList<T> {
            val offset = 0
            val childCount = viewGroup.childCount
            for (i in 0 until childCount) {
                val child = viewGroup.getChildAt(i + offset) ?: continue
                if (clazz.isAssignableFrom(child.javaClass)) {
                    list.add(child as T)
                } else if (child is ViewGroup) {
                    expand(child)
                }
            }
            return list
        }
    }
}