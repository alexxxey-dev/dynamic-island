package com.dynamic.island.oasis.dynamic_island.ui.features.quick_action

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutQuickActionBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.listeners.OutsideTouchListener
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.scaleClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickActionView(
    private val context: Context,
    private val viewModel: QuickActionViewModel,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutQuickActionBinding>(
    context,
    viewModel,
    R.layout.layout_quick_action,
    true,
    windowManager,
    inflater
) {
    override val registry= LifecycleRegistry(this)



    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        binding = createView()
        setupView(binding!!)
        expand(
            pivotY = 0f,
            diParams = diParams
        )

        Logs.view("show_quick_action_view")
    }


    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        collapse(
            pivotY = 0f,
            diParams = diParams
        )
        Logs.view("hide_quick_action_view")
        destroyView()
    }


    override fun setupView(binding: LayoutQuickActionBinding) {
        super.setupView(binding)
        binding.apply {
            camera.scaleClickListener {
                context.analyticsEvent("on_quick_action_camera")
                viewModel.openCamera()
            }
            screenshot.scaleClickListener {
                context.analyticsEvent("on_quick_action_screenshot")
                viewModel.takeScreenshot(context)
            }
            flashlight.scaleClickListener {
                context.analyticsEvent("on_quick_action_flashlight")
                viewModel.toggleFlashlight()
            }
            lock.scaleClickListener {
                context.analyticsEvent("on_quick_action_lock")
                viewModel.lockScreen()
            }
            settings.scaleClickListener {
                context.analyticsEvent("on_quick_action_settings")
                viewModel.openSettings()
            }
            root.setOnTouchListener(OutsideTouchListener(viewModel::hideView))
        }
        viewModel.screenshotVisible.observe(this) {
            binding?.screenshot?.visibility = if (it) View.VISIBLE else View.GONE
            binding?.flashlight?.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).launch {
            if (current !is DiState.QuickAction) {
                hide(current, previous, diParams)
                return@launch
            }

            show(current, previous, diParams)
        }

    }

}