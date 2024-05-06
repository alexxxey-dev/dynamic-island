package com.dynamic.island.oasis.dynamic_island.ui.features.music

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.session.MediaController
import android.os.Vibrator
import androidx.lifecycle.MutableLiveData
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.data.MyMusic
import com.dynamic.island.oasis.dynamic_island.data.MyPlaybackPosition
import com.dynamic.island.oasis.dynamic_island.data.MyPlaybackState
import com.dynamic.island.oasis.dynamic_island.listeners.media.MediaCallback
import com.dynamic.island.oasis.dynamic_island.listeners.media.PlaybackListener
import com.dynamic.island.oasis.dynamic_island.ui.OverlayViewModel
import com.dynamic.island.oasis.util.ext.isPlaying
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.dynamic_island.util.PhoneUtil
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.util.ext.doVibration
import kotlinx.coroutines.launch


class MusicViewModel(
    val di: DiViewModel,
    private val phone: PhoneUtil,
    private val vibrator: Vibrator,
    private val prefs: PrefsUtil,
    private val resources: Resources,
    private val packageManager: PackageManager,
    private val showIntent: (intent: Intent) -> Unit
) : OverlayViewModel(di) {
    private val mediaCallbacks = LinkedHashMap<String, MediaCallback>()


    val music = MutableLiveData<MyMusic?>()
    val playbackState = MutableLiveData<MyPlaybackState>()
    val playbackPosition = MutableLiveData<MyPlaybackPosition>()
    private var controlFromUser: Boolean = false

    private val positionListener = object : PlaybackListener() {
        override fun onPositionUpdated(position: MyPlaybackPosition) {
            playbackPosition.value = position
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        positionListener.stopListening()
        mediaCallbacks.clear()
    }

    fun onSessionAdded(controller: MediaController) = viewModelScope.launch {
        if (!prefs.appCompatible(controller.packageName)) {
            return@launch
        }
        if (mediaCallbacks.keys.contains(controller.packageName)) {
            return@launch
        }
        if (controller.packageName == phone.defaultCallApp() || controller.packageName == phone.callSessionPackage()) {
            return@launch
        }

        val callback = MediaCallback(
            packageManager = packageManager,
            controller = controller,
            viewModel = this@MusicViewModel,
            resources = resources
        )
        mediaCallbacks[controller.packageName] = callback
        Logs.log("onSessionAdded; ${controller.packageName}")
        Logs.log("${mediaCallbacks.values.map { it.controller.packageName }}")

        callback.updateMusic(controller.metadata, controller.playbackState)
    }

    fun onSessionRemoved(controller: MediaController) = viewModelScope.launch {
        val active = isActive(controller)
        mediaCallbacks.remove(controller.packageName)

        Logs.log("onSessionRemoved; ${controller.packageName}")
        Logs.log("${mediaCallbacks.values.map { it.controller.packageName }}")


        if (mediaCallbacks.isEmpty() || active) hideMusic()
    }

    fun activateController(controller: MediaController) = viewModelScope.launch {
        mediaCallbacks.getOrDefault(controller.packageName, null)?.let { temp ->
            mediaCallbacks.remove(controller.packageName)
            mediaCallbacks[controller.packageName] = temp
        }
    }

    fun showMusic(newMusic: MyMusic?, controller: MediaController) = viewModelScope.launch {
        if (newMusic == null) {
            return@launch
        }
        if (newMusic.id == music.value?.id) {
            return@launch
        }


        Logs.log("showMusic;  ${controller.packageName}")
        Logs.log("${mediaCallbacks.values.map { it.controller.packageName }}")


        positionListener.startListening(controller)
        music.value = newMusic
        showMusicState(controller)
    }

    fun hideMusic(controller: MediaController? = null) = viewModelScope.launch {
        positionListener.stopListening()
        music.value = null

        val musicDisplayed = di.state.value is DiState.Music
        if (musicDisplayed) {
            di.setState(DiState.Main())
        }
        di.hideBubble()
    }


    fun updatePlaybackState(newState: MyPlaybackState, controller: MediaController) =
        viewModelScope.launch {
            if (!isActive(controller) || !musicActive(controller)) return@launch

            newState.animate = controlFromUser
            playbackState.value = newState

        }

    private fun musicActive(controller: MediaController): Boolean {
        val currentMusic = music.value ?: return true
        val pn = controller.packageName ?: return true
        return pn == currentMusic.appPackage
    }

    fun isActive(controller: MediaController?): Boolean {
        if (controller == null) return true
        try {
            val activeController = mediaCallbacks.values.lastOrNull()?.controller ?: return false
            return activeController.packageName == controller.packageName
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }

    }

    private fun showMusicState(controller: MediaController) {
        val currentState = di.state.value
        val musicState = DiState.Music(
            expanded = if (currentState is DiState.Music) currentState.expanded else false,
            packageName = controller.packageName
        )

        val showBubble =
            currentState is DiState.ActiveCall || currentState is DiState.IncomingCall || currentState is DiState.Notification || currentState is DiState.Timer
        if (showBubble) {
            Logs.log("showMusicBubble")
            di.showBubble(musicState)
        } else if (currentState is DiState.Alert || currentState is DiState.QuickAction) {
            Logs.log("showMusicPrev")
            di.setPrevState(musicState)
        } else {
            Logs.log("showMusicState")
            di.setState(musicState)
        }
    }


    fun onLongClicked(showExpanded: Boolean): Boolean {
        val currentState = di.state.value
        if (currentState !is DiState.Music) return false

        if (showExpanded) vibrator.doVibration(Constants.LONG_CLICK_VIBRATION)
        di.setState(currentState.copy(expanded = showExpanded))
        return true
    }

    fun seekTo(position: Int) = viewModelScope.launch {
        val duration = music.value?.duration?.toDouble() ?: return@launch
        val controller = getMediaCallback()?.controller ?: return@launch

        if (!controller.playbackState.isPlaying()) {
            controller.transportControls.play()
            controlFromUser()
        }

        val positionMilliseconds = (duration / Constants.MAX_MUSIC_PROGRESS * position).toLong()
        controller.transportControls.seekTo(positionMilliseconds)
    }


    fun playPause() = viewModelScope.launch {
        val controller = getMediaCallback()?.controller ?: return@launch
        if (controller.playbackState.isPlaying()) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }

        controlFromUser()
    }


    private fun controlFromUser() {
        controlFromUser = true
        android.os.Handler().postDelayed({
            controlFromUser = false
        }, 250)
    }

    fun onStopClicked() {
        hideMusic()
    }


    private fun getMediaCallback(): MediaCallback? {
        val packageName = music.value?.appPackage ?: return null
        return mediaCallbacks.getOrDefault(packageName, null)
    }

    fun skipToNext(swipe: Boolean = false) = viewModelScope.launch {
        if (swipe && !prefs.settingEnabled(Constants.SET_SWIPE_TO_SKIP)) return@launch
        val controller = getMediaCallback()?.controller ?: return@launch
        if (!controller.playbackState.isPlaying()) {
            controller.transportControls.play()
        }

        controller.transportControls.skipToNext()
        controller.transportControls.seekTo(0)
    }

    fun skipToPrevious(swipe: Boolean = false) = viewModelScope.launch {
        if (swipe && !prefs.settingEnabled(Constants.SET_SWIPE_TO_SKIP)) return@launch
        val controller = getMediaCallback()?.controller ?: return@launch
        if (!controller.playbackState.isPlaying()) {
            controller.transportControls.play()
        }

        controller.transportControls.skipToPrevious()
        controller.transportControls.seekTo(0)
    }


    fun showMusicApp() = viewModelScope.launch {
        if (!prefs.settingEnabled(Constants.SET_CLICK_TO_OPEN)) return@launch
        val launchIntent = music.value?.launchIntent ?: return@launch
        showIntent(launchIntent)
    }


}