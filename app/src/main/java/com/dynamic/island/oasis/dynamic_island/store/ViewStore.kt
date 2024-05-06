package com.dynamic.island.oasis.dynamic_island.store

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.features.alert.AlertView
import com.dynamic.island.oasis.dynamic_island.ui.bubble.BubbleView
import com.dynamic.island.oasis.dynamic_island.ui.features.call.CollapsedCallView
import com.dynamic.island.oasis.dynamic_island.ui.features.call.ExpandedCallView
import com.dynamic.island.oasis.dynamic_island.ui.features.call.IncomingCallView
import com.dynamic.island.oasis.dynamic_island.ui.main.MainView
import com.dynamic.island.oasis.dynamic_island.ui.features.music.CollapsedMusicView
import com.dynamic.island.oasis.dynamic_island.ui.features.music.ExpandedMusicView
import com.dynamic.island.oasis.dynamic_island.ui.features.notification.CollapsedNotificationView
import com.dynamic.island.oasis.dynamic_island.ui.features.notification.ExpandedNotificationView
import com.dynamic.island.oasis.dynamic_island.ui.features.quick_action.QuickActionView
import com.dynamic.island.oasis.dynamic_island.ui.features.timer.CollapsedTimerView
import com.dynamic.island.oasis.dynamic_island.ui.features.timer.ExpandedTimerView
import com.dynamic.island.oasis.dynamic_island.ui.features.screenshot.ScreenshotView
import com.dynamic.island.oasis.util.ext.analyticsEvent


class ViewStore(
    private val context: Context,
    private val viewModelStore: ViewModelStore,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) :LifecycleOwner{
    private var previousState: DiState? = null

    private val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = registry

    private val screenshotView = ScreenshotView(windowManager,inflater)
    private val bubbleView = BubbleView(viewModelStore,context,windowManager,inflater)
    private val mainView = MainView( context, viewModelStore.mainViewModel,windowManager,inflater)
    private val alertView = AlertView( context, viewModelStore.alertViewModel,windowManager,inflater)
    private val collapsedMusicView =
        CollapsedMusicView( context, viewModelStore.musicViewModel,windowManager,inflater)
    private val expandedMusicView =
        ExpandedMusicView( context, viewModelStore.musicViewModel,windowManager,inflater)
    private val quickActionView =
        QuickActionView( context, viewModelStore.quickActionViewModel,windowManager,inflater)
    private val incomingCallView = IncomingCallView( viewModelStore.callViewModel, context,windowManager,inflater)
    private val collapsedCallView = CollapsedCallView(viewModelStore.callViewModel,context,windowManager,inflater)
    private val expandedCallView = ExpandedCallView(viewModelStore.callViewModel, context,windowManager,inflater)
    private val collapsedTimerView = CollapsedTimerView(viewModelStore.timerViewModel,context,windowManager,inflater)
    private val expandedTimerView = ExpandedTimerView(context, viewModelStore.timerViewModel,windowManager,inflater)
    private val collapsedNotificationView = CollapsedNotificationView(context,viewModelStore.notificationViewModel,windowManager,inflater)
    private val expandedNotificationView = ExpandedNotificationView(context,viewModelStore.notificationViewModel,windowManager,inflater)

    private val views =
        listOf(
            mainView,
            alertView,
            collapsedMusicView,
            expandedMusicView,
            quickActionView,
            incomingCallView,
            collapsedCallView,
            expandedCallView,
            collapsedTimerView,
            expandedTimerView,
            collapsedNotificationView,
            expandedNotificationView,
            bubbleView
        )

    init {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        observeState()
    }

    fun onDestroy() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        screenshotView.hide()
        views.forEach { it.onDestroy() }
    }

    private fun observeState() {
        viewModelStore.diViewModel.showAppScreenshot.observe(this){
            screenshotView.show(it)
        }
        viewModelStore.diViewModel.hideAppScreenshot.observe(this){
            screenshotView.hide()
        }
        viewModelStore.diViewModel.state.observe(this) {
            if(it==previousState) {
                return@observe
            }
            drawState(it)
            previousState = it

        }

    }

    private fun drawState(state: DiState) {
        context.analyticsEvent("draw_di_state", bundleOf(
            "class_name" to state.javaClass.name
        ))
        Logs.log("draw state = $state")
        val params = viewModelStore.diViewModel.provideParams()
        views.forEach { it.drawState(state, previousState, params) }
    }




}