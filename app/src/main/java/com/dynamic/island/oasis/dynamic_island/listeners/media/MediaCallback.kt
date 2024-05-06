package com.dynamic.island.oasis.dynamic_island.listeners.media

import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.MyInteractive
import com.dynamic.island.oasis.dynamic_island.data.MyMusic
import com.dynamic.island.oasis.dynamic_island.data.MyPlaybackState
import com.dynamic.island.oasis.dynamic_island.ui.features.music.MusicViewModel
import com.dynamic.island.oasis.util.ext.isPlaying
import com.dynamic.island.oasis.util.ext.getAppIntent
import com.dynamic.island.oasis.util.ext.getAppLogo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaCallback(
    private val packageManager: PackageManager,
    val controller: MediaController,
    private val viewModel: MusicViewModel,
    private val resources: Resources
) : MediaController.Callback() {


    init {
        controller.registerCallback(this)
    }

    override fun onSessionDestroyed() {
        super.onSessionDestroyed()
        controller.unregisterCallback(this)
        viewModel.onSessionRemoved(controller)
    }

    override fun onPlaybackStateChanged(playbackState: PlaybackState?) {
        super.onPlaybackStateChanged(playbackState)
        Logs.log("onPlaybackStateChanged")
        updateMusic(controller.metadata, playbackState)
    }

    override fun onMetadataChanged(metadata: MediaMetadata?) {
        super.onMetadataChanged(metadata)
        updateMusic(metadata, controller.playbackState)
    }

    fun updateMusic(metadata: MediaMetadata? = null, playbackState: PlaybackState? = null) = CoroutineScope(Dispatchers.IO).launch{
        mapMediaMetadata(
            metadata = metadata,
            isInteractive = isInteractive(playbackState)
        )?.let {
            if(playbackState?.isPlaying() == true){
                viewModel.activateController(controller)
                viewModel.showMusic(it, controller)
            }
        }

        playbackState?.let {
            if (isStopped(playbackState) && viewModel.isActive(controller)) {
                viewModel.hideMusic(controller)
                return@launch
            }

            val state = mapPlaybackState(playbackState)
            viewModel.updatePlaybackState(state, controller)
        }
    }

    private fun isStopped(state:PlaybackState) = state.state == PlaybackState.STATE_STOPPED || state.state == PlaybackState.STATE_NONE

    private fun mapMediaMetadata(
        metadata: MediaMetadata?,
        isInteractive: MyInteractive?
    ): MyMusic? {
        if (metadata == null) {
            return null
        }

        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val appLogo = packageManager.getAppLogo(controller.packageName)
        val albumLogo = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val launchIntent = packageManager.getAppIntent(controller.packageName)
        val appPackage = controller.packageName

        if (duration == 0L && albumLogo == null && title.isNullOrBlank() && artist.isNullOrBlank()) {
            return null
        }

        return MyMusic(
            id = metadata.hashCode(),
            duration = duration,
            appLogo = appLogo,
            albumLogo = albumLogo,
            title = title ?: resources.getString(R.string.unknown),
            artist = artist ?: resources.getString(R.string.unknown),
            launchIntent = launchIntent,
            appPackage = appPackage,
            isInteractive = isInteractive ?: MyInteractive()
        )
    }

    private fun mapPlaybackState(state: PlaybackState): MyPlaybackState {
        return MyPlaybackState(
            isActive = state.isPlaying()
        )
    }


    private fun isInteractive(state: PlaybackState?): MyInteractive? {
        if(state ==null) return null
        val canSkipNext =
            state.actions and PlaybackState.ACTION_SKIP_TO_NEXT == PlaybackState.ACTION_SKIP_TO_NEXT
        val canSkipPrevious =
            state.actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS == PlaybackState.ACTION_SKIP_TO_PREVIOUS
        val canSeek = state.actions and PlaybackState.ACTION_SEEK_TO == PlaybackState.ACTION_SEEK_TO
        val canPausePlay =
            state.actions and PlaybackState.ACTION_PLAY_PAUSE == PlaybackState.ACTION_PLAY_PAUSE
        return MyInteractive(canSkipNext, canSkipPrevious, canPausePlay, canSeek)
    }

}