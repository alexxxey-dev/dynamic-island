package com.dynamic.island.oasis.dynamic_island.ui.bubble

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutBubbleBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiBackground
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.store.ViewModelStore
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.util.ext.safeUpdateView
import com.dynamic.island.oasis.util.ext.getAppLogo
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class BubbleView(
    private val viewModelStore: ViewModelStore,
    private val context: Context,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutBubbleBinding>(
    context, viewModelStore.bubbleViewModel, R.layout.layout_bubble, false,windowManager, inflater
) {
    override val registry= LifecycleRegistry(this)

    private var visible: Boolean = false

    init {
        binding = createView()
        setupView(binding!!)
        Logs.view("show_bubble_view")
    }


    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {}
    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {}
    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {}

    override fun onDestroy() {
        super.onDestroy()
        Logs.view("hide_bubble_view")
    }


    private fun onParamsUpdated(diParams: DiParams) {
        val margin = context.resources.getDimension(R.dimen.di_bubble_margin)
        val padding = context.resources.getDimension(R.dimen.di_default_padding)
        val mHeight = diParams.height - padding
        val mWidth = diParams.width - padding * 2
        val endX = (diParams.x + mWidth / 2 + margin + mHeight / 2).toInt()

        if (visible) {
             setDiParams(diParams.copy(x = endX))
        } else{
            setDiParams(diParams.copy(y = 0- diParams.height))
        }
    }

    override fun updateBackground(diBackground: DiBackground) {
        try {
            val background = binding?.background
            background?.setBackgroundResource(R.drawable.shape_di_bubble)
            (background?.background as GradientDrawable).setColor(diBackground.color)
        }catch (ex:Exception){
        }

    }

    fun show(state: DiState) {
        viewJob = CoroutineScope(Dispatchers.Main).safeLaunch {
            if (viewModelStore.diViewModel.bubbleState.value != state) {
                setDiParams(viewModelStore.diViewModel.params.value)
                binding?.root?.visibility = View.VISIBLE
                showViews(state)
                addAnimation()
                visible = true
            }
            viewModelStore.diViewModel.bubbleState.value = state
        }

    }

    fun hide() {
        viewJob = CoroutineScope(Dispatchers.Main).safeLaunch {
            if (viewModelStore.diViewModel.bubbleState.value != null) {
                removeAnimation()
                hideViews()
                binding?.root?.visibility = View.GONE
                setDiParams(DiParams(layout.width, layout.height, layout.x,  0 - layout.height))
                visible = false
                viewModelStore.diViewModel.bubbleState.value = null
            }
        }
    }


    override fun setDiParams(diParams: DiParams?) {
        if (diParams == null) return
        val padding = context.resources.getDimension(R.dimen.di_default_padding)
        val size = (diParams.height - padding).toInt()

        windowManager.safeUpdateView(binding?.root, layout.apply {
            x = diParams.x
            y = diParams.y
            width = size
            height = size
        })
    }

    private fun hideViews() {
        binding?.let { binding ->
            binding.layoutCall.visibility = View.GONE
            binding.notificationImage.visibility = View.GONE
            binding.timerAnimation.visibility = View.GONE
            binding.musicImage.visibility = View.GONE
        }
    }

    private fun showViews(state: DiState) {
        binding?.let { binding ->
            when (state) {
                is DiState.Music -> {
                    binding.layoutCall.visibility = View.GONE
                    binding.notificationImage.visibility = View.GONE
                    binding.timerAnimation.visibility = View.GONE

                    binding.musicImage.visibility = View.VISIBLE
                }

                is DiState.ActiveCall, is DiState.IncomingCall -> {
                    binding.notificationImage.visibility = View.GONE
                    binding.timerAnimation.visibility = View.GONE
                    binding.musicImage.visibility = View.GONE

                    binding.layoutCall.visibility = View.VISIBLE
                }

                is DiState.Timer -> {
                    binding.layoutCall.visibility = View.GONE
                    binding.notificationImage.visibility = View.GONE
                    binding.musicImage.visibility = View.GONE

                    binding.timerAnimation.visibility = View.VISIBLE
                    binding.timerAnimation.pauseAnimation()
                    binding.timerAnimation.progress = state.progress
                    if (viewModelStore.timerViewModel.timerActive.value == true) {
                        binding.timerAnimation.resumeAnimation()
                    }
                }

                is DiState.Notification -> {
                    binding.layoutCall.visibility = View.GONE
                    binding.timerAnimation.visibility = View.GONE
                    binding.musicImage.visibility = View.GONE

                    binding.notificationImage.visibility = View.VISIBLE
                }

                else -> {}
            }
        }
    }

    override fun setupView(binding: LayoutBubbleBinding) {
        binding?.root?.setOnClickListener {
            viewModelStore.bubbleViewModel.openApp()
        }
        binding?.timerAnimation?.apply {
            setMinProgress(0f)
            setMaxProgress(1f)
            speed = 1f / 30f
            playAnimation()
        }

        viewModelStore.diViewModel.bubbleState.observe(this) { current ->
            if (current is DiState.Timer) {
                binding?.timerAnimation?.pauseAnimation()
                binding?.timerAnimation?.progress = current.progress
                if (viewModelStore.timerViewModel.timerActive.value == true) {
                    binding?.timerAnimation?.resumeAnimation()
                }
            }
        }
        viewModelStore.timerViewModel.timerActive.observe(this) { active ->
            updateTimerState(active)
        }
        viewModelStore.diViewModel.params.observe(this) {
            onParamsUpdated(it)
        }
        viewModelStore.diViewModel.visible.observe(this) {
            binding?.root?.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModelStore.diViewModel.background.observe(this) {
            updateBackground(it)
        }
        viewModelStore.notificationViewModel.collapsedNotification.observe(this) {
            if (it == null) return@observe
            val logo = context.packageManager.getAppLogo(it.packageName) ?: return@observe
            binding?.notificationImage?.setImageDrawable(logo)
        }
        viewModelStore.musicViewModel.music.observe(this) {
            if (it == null) return@observe
            if (it.albumLogo != null) {
                binding?.musicImage?.setImageBitmap(it.albumLogo)
            } else {
                binding?.musicImage?.setImageResource(R.drawable.ic_default_album)
            }
        }
        viewModelStore.callViewModel.contact.observe(this) {
            if (it == null) return@observe
            if (it.photo != null) {
                binding?.layoutCallText?.visibility = View.GONE
                binding?.callImage?.visibility = View.VISIBLE
                binding?.callImage?.setImageBitmap(it.photo)
            } else {
                binding?.layoutCallText?.visibility = View.VISIBLE
                binding?.callImage?.visibility = View.GONE
                binding?.callText?.setText(it.letter)
            }
        }
        viewModelStore.diViewModel.hideBubble.observe(this) {
            hide()
        }
        viewModelStore.diViewModel.showBubble.observe(this) {
            show(it)
        }
    }

    private suspend fun addAnimation() = suspendCancellableCoroutine<Unit> { continuation ->
        val it = viewModelStore.diViewModel.params.value ?: return@suspendCancellableCoroutine
        val margin = context.resources.getDimension(R.dimen.di_bubble_margin)
        val padding = context.resources.getDimension(R.dimen.di_default_padding)
        val mHeight = it.height - padding
        val mWidth = it.width - padding * 2
        val endX = (it.x + mWidth / 2 + margin + mHeight / 2).toInt()
        val startX = layout.x.toInt()

        val animation = ObjectAnimator.ofInt(startX, endX).apply {
            addUpdateListener {
                windowManager.safeUpdateView(binding?.root, layout.apply {
                    val mX = it.animatedValue as Int
                    x = mX
                })
            }
            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    if(continuation.isActive) continuation.resume(Unit)
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }

            })
            interpolator = OvershootInterpolator()
            duration = Constants.BUBBLE_DURATION
        }
        animation.start()
    }


    private suspend fun removeAnimation() = suspendCancellableCoroutine<Unit> { continuation ->
        val it = viewModelStore.diViewModel.params.value ?: return@suspendCancellableCoroutine
        val endX = it.x
        val startX = layout.x.toInt()
        val animation = ObjectAnimator.ofInt(startX, endX).apply {
            addUpdateListener {
                windowManager.safeUpdateView(binding?.root, layout.apply {
                    val mX = it.animatedValue as Int
                    x = mX
                })
            }
            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    if(continuation.isActive) continuation.resume(Unit)
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }

            })
            interpolator = DecelerateInterpolator()
            duration = Constants.BUBBLE_DURATION
        }
        animation.start()

    }


    fun updateTimerState(active: Boolean) {
        val animation = binding?.timerAnimation ?: return

        if (active) {
            animation.resumeAnimation()
        } else {
            animation.pauseAnimation()
        }
    }


}