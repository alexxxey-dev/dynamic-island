package com.dynamic.island.oasis.dynamic_island.ui.features.timer

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutExpandedTimerBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.listeners.OutsideTouchListener
import com.dynamic.island.oasis.util.ext.analyticsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpandedTimerView(
    private val context: Context,
    private val viewModel: TimerViewModel,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : TimerView<LayoutExpandedTimerBinding>(
    context,
    viewModel,
    R.layout.layout_expanded_timer,
    true,windowManager,inflater
){
    override val registry= LifecycleRegistry(this)



    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        val same = (previous is DiState.Timer && previous.expanded)
        if(same) return

        Logs.view("show_expanded_timer_view")
        binding = createView()
        setupView(binding!!)
        expand(diParams = diParams)
    }

    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        Logs.view("hide_expanded_timer_view")
        collapse(diParams = diParams)
        destroyView()
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        super.drawState(current, previous, diParams)
        viewJob = CoroutineScope(Dispatchers.Main).launch {
            if (current !is DiState.Timer || !current.expanded) {
                hide(current, previous, diParams)
                return@launch
            }
            show(current, previous, diParams)
        }
    }


    override fun setupView(binding: LayoutExpandedTimerBinding) {
        super.setupView(binding)
        initAnimation()
        binding?.apply {
            root.setOnClickListener { viewModel.openTimerApp() }
            root.setOnTouchListener(OutsideTouchListener { viewModel.collapseTimer(binding?.animation?.progress) })
        }
        viewModel.notifActions.observe(this){
            showNotifActions(it){action->
                context.analyticsEvent("on_timer_action_clicked")
                viewModel.executeNotifAction(action)
            }
        }
        viewModel.time.observe(this){
            binding?.time?.text = it
        }
    }
}