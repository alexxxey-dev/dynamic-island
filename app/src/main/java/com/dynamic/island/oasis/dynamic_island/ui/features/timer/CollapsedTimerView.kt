package com.dynamic.island.oasis.dynamic_island.ui.features.timer

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutCollapsedTimerBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.util.ext.showAlphaCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CollapsedTimerView(
    private val viewModel: TimerViewModel,
    private val context: Context,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : TimerView<LayoutCollapsedTimerBinding>(
    context,
    viewModel,
    R.layout.layout_collapsed_timer,
    false,windowManager, inflater
) {
    override val registry= LifecycleRegistry(this)



    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        val same = (previous is DiState.Timer && !previous.expanded)
        if(same) return

        Logs.view("show_collapsed_timer_view")
        binding = createView()
        setupView(binding!!)
        binding?.backgroundSecond?.showAlphaCoroutine(Constants.COLLAPSE_TIME)
    }

    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        Logs.view("hide_collapsed_timer_view")
        destroyView()
    }
    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        super.drawState(current, previous, diParams)
        viewJob = CoroutineScope(Dispatchers.Main).launch {
            if (current !is DiState.Timer || current.expanded) {
                hide(current, previous, diParams)
                return@launch
            }

           show(current, previous, diParams)
        }

    }




    override fun setupView(binding: LayoutCollapsedTimerBinding) {
        super.setupView(binding)
        initAnimation()
        binding?.apply {
            root.setOnLongClickListener { viewModel.onLongClicked(binding?.animation?.progress) }
            root.setOnClickListener { viewModel.openTimerApp() }
        }
        viewModel.time.observe(this) {
            binding?.duration?.text = it
        }
    }
}