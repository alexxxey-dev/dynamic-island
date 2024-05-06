package com.dynamic.island.oasis.dynamic_island.listeners

import android.os.Handler
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.listeners.media.MediaCallback
import com.dynamic.island.oasis.util.ext.toDurationString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class CallTimeListener {
    private var job: Job? = null
    private var startTime:Long? = null

    abstract fun onTimeUpdated(time:String)

    private fun start() = CoroutineScope(Dispatchers.IO).launch {
        while(this.isActive){
            delay(Constants.PLAYBACK_LISTENER_DELAY)
            withContext(Dispatchers.Main) { onTimeUpdated(getTimeString()) }
        }
    }



    fun startListening() {
        onTimeUpdated("00:00")
        startTime = System.currentTimeMillis()
        job = start()
    }


    fun stopListening() {
        startTime = null
        job?.cancel()
        job = null
    }

    private fun getTimeString(): String {
        if(startTime==null) return "00:00"
        val difference = System.currentTimeMillis() - startTime!!
        return difference.toDurationString()
    }

}