package com.dynamic.island.oasis.util.ext

import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.PlaybackState
import com.dynamic.island.oasis.dynamic_island.Logs


fun PlaybackState?.isPlaying():Boolean{
    if(this==null) return false
    return  when (this.state) {
        PlaybackState.STATE_FAST_FORWARDING,
        PlaybackState.STATE_REWINDING,
        PlaybackState.STATE_SKIPPING_TO_PREVIOUS,
        PlaybackState.STATE_SKIPPING_TO_NEXT,
        PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM,
        PlaybackState.STATE_BUFFERING,
        PlaybackState.STATE_CONNECTING,
        PlaybackState.STATE_PLAYING -> true
        else -> false
    }
}
fun MediaMetadata?.getString(key:String):String?{
    if(this==null) return null
    return try {
        this.getString(key  )
    }catch (ex:Exception){
        Logs.log("getString exception")
        Logs.exception(ex)
        null
    }
}
fun MediaMetadata?.getLong(key:String):Long?{
    if(this==null) return null
    return try {
        this.getLong(key  )
    }catch (ex:Exception){
        Logs.log("getLong exception")
        Logs.exception(ex)
        null
    }
}


fun MediaMetadata?.getBitmap(key:String):Bitmap?{
    if(this==null) return null
    return try {
        this.getBitmap(key)
    }catch (ex:Exception){
        Logs.log("getBitmap exception")
        Logs.exception(ex)
        null
    }
}
