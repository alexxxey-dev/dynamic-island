package com.dynamic.island.oasis.dynamic_island.ui.features.alert

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutAlertBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.util.ext.safeRemoveView
import com.dynamic.island.oasis.util.ext.play
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class AlertView(
    private val context: Context,
    private val viewModel: AlertViewModel,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutAlertBinding>(
    context,
    viewModel,
    R.layout.layout_alert,
    true,
    windowManager,
    inflater
) {
    override val registry= LifecycleRegistry(this)



    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        if(current !is DiState.Alert) return


        val same = previous is DiState.Alert
        if (!same) {
            Logs.view("show_alert_view")
            binding = createView()
             setupView(binding!!)
            showAlert(current.text, current.animation)
            expand(
                startX = 0.3f,
                startY = 0.5f,
                pivotY = 0f,
                diParams = diParams
            )
        } else{
            showAlert(current.text, current.animation)
        }

        binding?.animation.play()
        viewModel.hideAlertState()
    }


    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        collapse(
            pivotY = 0f,
            diParams = diParams
        )
        Logs.view("hide_alert_view")
        destroyView()
    }

    override fun setupView(binding: LayoutAlertBinding) {
        super.setupView(binding)
        binding.animation.speed = 2f
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).safeLaunch {
            if (current !is DiState.Alert) {
                hide(current, previous, diParams)
                return@safeLaunch
            }

            show(current, previous, diParams)
        }
    }


    private fun showAlert(text: String, animation: Int) {
        binding?.let { binding ->
            binding.text.text = text
            binding.animation.removeAllAnimatorListeners()
            binding.animation.frame = 0
            binding.animation.setAnimation(animation)
        }
    }



}