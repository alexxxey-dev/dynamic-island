package com.dynamic.island.oasis.dynamic_island.ui

import android.app.Notification
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.airbnb.lottie.LottieAnimationView
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.data.DiBackground
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.util.ext.safeRemoveView
import com.dynamic.island.oasis.util.ext.safeUpdateView
import com.dynamic.island.oasis.util.ext.showNotifActions
import com.dynamic.island.oasis.util.ext.xToPercent
import com.dynamic.island.oasis.util.ext.collapse
import com.dynamic.island.oasis.util.ext.expand
import com.dynamic.island.oasis.util.ext.safeAddView
import kotlinx.coroutines.Job


abstract class OverlayView<V : ViewDataBinding>(
    private val context: Context,
    private val viewModel: OverlayViewModel,
    private val layoutResource: Int,
    private val expanded: Boolean,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
):LifecycleOwner {
    abstract val registry:LifecycleRegistry
    override val lifecycle: Lifecycle
        get() = registry



    open val layout = if (expanded) expandedLayout() else collapsedLayout()
    var binding: V? = null
    var viewJob: Job? = null



    abstract fun drawState(current: DiState, previous: DiState?, diParams: DiParams)

    abstract suspend fun show(current: DiState, previous: DiState?, diParams: DiParams)

    abstract suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams)

    fun createView(): V {
        val mBinding = DataBindingUtil.inflate<V>(
            inflater, layoutResource,
            null, false
        )
        binding = mBinding
        setDiParams(viewModel.vm.params.value)
        windowManager.safeAddView(binding?.root, layout)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return mBinding
    }

    fun destroyView() {
        if(binding==null) return
        windowManager.safeRemoveView(binding?.root)
        binding = null
        if(registry.currentState.isAtLeast(Lifecycle.State.CREATED)){
            registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    open fun setupView(binding:V) {
        viewModel.vm.visible.observe(this) {
            binding.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.vm.params.observe(this) {
            setDiParams(it)
        }
        viewModel.vm.background.observe(this) {
            updateBackground(it)
        }
    }

    open fun setDiParams(newParams: DiParams?) {
        if(newParams==null) return
        if (expanded) {
            layout.y = newParams.y
        } else{
            layout.apply {
                x = newParams.x
                y = newParams.y
                width = newParams.width
                height = newParams.height
            }
        }

        if (binding == null) return
        windowManager.safeUpdateView(binding?.root, layout)
    }


    open fun onDestroy() {
        if (viewJob?.isActive == true) viewJob?.cancel()
        binding?.root?.findViewById<LottieAnimationView>(R.id.animation)?.cancelAnimation()
        binding?.root?.findViewById<LottieAnimationView>(R.id.timerAnimation)?.cancelAnimation()
        binding?.root?.findViewById<LottieAnimationView>(R.id.fake_visualizer)?.cancelAnimation()
        binding?.root?.findViewById<LottieAnimationView>(R.id.playPause)?.cancelAnimation()
        destroyView()
    }

    fun showNotifActions(
        actions: List<Notification.Action>?,
        onActionClicked: (Notification.Action) -> Unit
    ) {
        try {
            val layoutActions =
                binding?.root?.findViewById<LinearLayout>(R.id.layoutActions) ?: return
            context.showNotifActions(layoutActions, actions, onActionClicked)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    open fun updateBackground(diBackground: DiBackground) {
        try {
            val background = binding?.root?.findViewById<View>(R.id.background)
            if (expanded) {
                background?.setBackgroundResource(R.drawable.shape_di_rounded)
            } else {
                background?.setBackgroundResource(diBackground.bgRes)
            }
            (background?.background as GradientDrawable).setColor(diBackground.color)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        try {
            val backgroundSecond =
                binding?.root?.findViewById<View>(R.id.backgroundSecond)
            if (expanded) {
                backgroundSecond?.setBackgroundResource(R.drawable.shape_di_rounded)
            } else {
                backgroundSecond?.setBackgroundResource(diBackground.bgRes)
            }
            (backgroundSecond?.background as GradientDrawable).setColor(diBackground.color)
        } catch (ex: Exception) {

        }

    }


    private fun expandedLayout(): WindowManager.LayoutParams {
        val collapsedParams = collapsedLayout()
        return WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            type = collapsedParams.type
            format = collapsedParams.format
            flags = collapsedParams.flags
            x = 0
            y = collapsedParams.y
        }
    }

    private fun collapsedLayout() = WindowManager.LayoutParams().apply {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.TRANSLUCENT
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    }


    fun setFocusable(focusable: Boolean) {
        if (binding == null) return
        //TODO
//        val focusableFlags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
//                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//        val notFocusableFlags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
//                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//        windowManager.safeUpdateView(binding?.root, layout.apply {
//            flags = if (focusable) focusableFlags else notFocusableFlags
//        })
    }

    suspend fun collapse(
        pivotY: Float = 0f,
        toX: Float = 0f,
        toY: Float = 0f,
        diParams: DiParams
    ) {
        if (binding == null) return
        val backgroundMain =
            binding?.root?.findViewById<View>(R.id.background) ?: return
        try {
            backgroundMain.collapse(
                time = Constants.COLLAPSE_TIME,
                pivotX = windowManager.xToPercent(diParams.x, diParams.width),
                pivotY = pivotY,
                toX = toX,
                toY = toY
            )
        } catch (ex: Exception) {
            Logs.log("collapse")
            Logs.exception(ex)
        }

    }


    suspend fun expand(
        diParams: DiParams,
        startX: Float = 0f,
        startY: Float = 0f,
        pivotY: Float = 0f
    ) {
        if (binding == null) {
            return
        }
        val backgroundMain =
            binding?.root?.findViewById<View>(R.id.background) ?: return
        try {
            val time = Constants.EXPAND_TIME
            val backgroundSecond =
                binding?.root?.findViewById<View>(R.id.backgroundSecond)
            backgroundSecond?.startAnimation(AlphaAnimation(0f, 1f).apply {
                duration = time
                fillAfter = true
            })
            val pivotX = windowManager.xToPercent(diParams.x, diParams.width)
            backgroundMain.expand(
                startX = startX,
                startY = startY,
                pivotX = pivotX,
                pivotY = pivotY,
                time = time
            )
        } catch (ex: Exception) {
            Logs.log("expand exception")
            Logs.exception(ex)
        }

    }

}