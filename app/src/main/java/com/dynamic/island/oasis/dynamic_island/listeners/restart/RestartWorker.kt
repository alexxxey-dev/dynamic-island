package com.dynamic.island.oasis.dynamic_island.listeners.restart

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.service.ServiceWrapper
import com.dynamic.island.oasis.dynamic_island.service.MainService




class RestartWorker(private val context: Context, private val params: WorkerParameters) : Worker(context, params) {
    companion object{
        const val RESTART_PERIOD_MIN = 16L
        const val RESTART_WORK_NAME = "RESTART_WORK"
    }

    override fun doWork(): Result {
        startService()
        return Result.success()
    }


    private fun startService() {
        val prefs= context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val prefsUtil = PrefsUtil(context, prefs)

        if (!prefsUtil.serviceEnabled()) return
        if (MainService.isRunning(context))  return

        val intent = Intent(context, MainService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
}