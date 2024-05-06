package com.dynamic.island.oasis.dynamic_island.listeners.media

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Handler
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.data.MyPlaybackPosition
import com.dynamic.island.oasis.util.ext.toDurationString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


abstract class PlaybackListener() {
    private var job: Job? = null
    private var controller: MediaController? = null

    abstract fun onPositionUpdated(position: MyPlaybackPosition)

    private fun start() = CoroutineScope(Dispatchers.IO).launch {
        while(this.isActive){
           delay(Constants.PLAYBACK_LISTENER_DELAY)
            mapPlaybackState(controller?.playbackState)?.let {
                withContext(Dispatchers.Main) { onPositionUpdated(it) }
            }
        }
    }


    fun startListening(controller: MediaController) {
        this.controller = controller
        job = start()
    }


    fun stopListening() {
        controller = null
        job?.cancel()
        job = null
    }

    private fun mapPlaybackState(playbackState: PlaybackState?): MyPlaybackPosition? {
        if(playbackState==null) return null
        val duration = controller?.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val totalTime: String
        val currentTime: String
        val progress: Int
        if (duration == null || duration <= 0L) {
            totalTime = "00:00"
            currentTime = "00:00"
            progress = 0
        } else {
            totalTime = duration.toDurationString()
            currentTime = playbackState.position.toDurationString()
            progress =
                (playbackState.position.toDouble() / duration.toDouble() * Constants.MAX_MUSIC_PROGRESS).toInt()
        }



        return MyPlaybackPosition(currentTime, totalTime, progress)
    }


}