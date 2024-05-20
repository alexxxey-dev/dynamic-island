package com.dynamic.island.oasis.dynamic_island.listeners.restart

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dynamic.island.oasis.dynamic_island.service.MainService
import com.dynamic.island.oasis.dynamic_island.service.ServiceWrapper

class BootReceiver :BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MainService.startViaWorker(context)
    }
}