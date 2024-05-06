package com.dynamic.island.oasis.dynamic_island.ui.features.timer

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView

abstract class TimerView<T : ViewDataBinding>(
    private val context: Context,
    private val viewModel: TimerViewModel,
    private val layoutResource: Int,
    private val expanded: Boolean,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<T>(
     context, viewModel, layoutResource, expanded,windowManager,inflater
) {
    private var pauseProgress:Float? = null

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        if (current !is DiState.Timer) return
        animation()?.pauseAnimation()
        animation()?.progress = current.progress

        if(viewModel.timerActive.value == true) {
            animation()?.resumeAnimation()
        }

    }


    private fun updateTimerState(active:Boolean){
         val animation = animation() ?: return

        if(active) {
            animation.resumeAnimation()
        } else {
            animation.pauseAnimation()
        }
    }

    override fun setupView(binding: T) {
        super.setupView(binding)
        viewModel.timerActive.observe(this){ active->
            updateTimerState(active)
        }
    }

     fun initAnimation() {
        animation()?.apply {
            setMinProgress(0f)
            setMaxProgress(1f)
            speed = 1f / 30f
            playAnimation()
        }
    }

    private fun animation() = try {
        val mAnimation = binding?.root?.findViewById<LottieAnimationView>(R.id.animation)
        mAnimation
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }
}