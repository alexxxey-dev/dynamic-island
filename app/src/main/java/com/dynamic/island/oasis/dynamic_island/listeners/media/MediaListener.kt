package com.dynamic.island.oasis.dynamic_island.listeners.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import com.dynamic.island.oasis.dynamic_island.listeners.notifications.NotificationListener
import com.dynamic.island.oasis.dynamic_island.ui.features.music.MusicViewModel


class MediaListener(
    private val context: Context,
    private val mediaManager: MediaSessionManager,
    private val musicViewModel: MusicViewModel
) {


    private val listener = OnActiveSessionsChangedListener { controllers ->
        controllers?.forEach { musicViewModel.onSessionAdded(it) }
    }

    init {
        val component = ComponentName(context, NotificationListener::class.java)
        mediaManager.addOnActiveSessionsChangedListener(listener, component)
        val activeSessions = mediaManager.getActiveSessions(component)
        activeSessions.forEach { musicViewModel.onSessionAdded(it) }
    }

    fun onDestroy() {
        mediaManager.removeOnActiveSessionsChangedListener(listener)
    }
}