package com.dynamic.island.oasis.ui.animated_switch

import android.animation.Animator
import android.content.Context
import android.os.Vibrator
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.util.ext.doVibration


class AnimatedSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    var isChecked: Boolean = false
        private set

    private var mAnimation: LottieAnimationView? = null
    private var mImage: ImageView? = null
    private var checkedListener: AnimatedSwitchListener? = null
    private val vibrator = context.applicationContext.getSystemService(Vibrator::class.java)

    init {
        addViews()
        setOnClickListener { onClicked() }
        setChecked(false, false)
        cacheAnimations()
    }

    fun onClicked(){
        setChecked(!isChecked, true)
    }

    fun setCheckedListener(listener: AnimatedSwitchListener) {
        this.checkedListener = listener
    }

    fun setChecked(checked: Boolean, fromUser: Boolean) {
        checkedListener?.onCheckedChanged(checked,fromUser)

        if (fromUser) {
            vibrator.doVibration(Constants.CLICK_VIBRATION)
            animation(checked)
        } else {
            noAnimation(checked)
        }

        isChecked = checked
    }

    private fun addViews() {
        mAnimation = lottieAnimation()
        mImage = imageView()
        addView(mAnimation)
        addView(mImage)
    }


    private fun lottieAnimation() = LottieAnimationView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        visibility = View.GONE
        speed = Constants.SWITCH_SPEED
    }
    private fun imageView() = ImageView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        visibility= View.GONE
    }

    private fun cacheAnimations() {
        mAnimation?.setAnimation(R.raw.switch_on_off)
        mAnimation?.setAnimation(R.raw.switch_off_on)
    }

    private fun animation(checked: Boolean) {
        val animationRes = getAnimationRes(checked)
        val imageRes = getImageRes(checked)
        mAnimation?.removeAllLottieOnCompositionLoadedListener()
        mAnimation?.clearAnimation()
        mAnimation?.setAnimation(animationRes)
        mAnimation?.addLottieOnCompositionLoadedListener {
            mAnimation?.removeAllLottieOnCompositionLoadedListener()

            mImage?.visibility = View.GONE
            mAnimation?.visibility = View.VISIBLE

            mAnimation?.playAnimation()
            mAnimation?.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    mAnimation?.removeAnimatorListener(this)

                    mImage?.visibility = View.VISIBLE
                    mAnimation?.visibility = View.GONE

                    mImage?.setImageResource(imageRes)

                }

                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }

    private fun getAnimationRes(checked: Boolean) =
        if (checked) R.raw.switch_off_on else R.raw.switch_on_off

    private fun getImageRes(checked: Boolean) =
        if (checked) R.drawable.ic_switch_on else R.drawable.ic_switch_off

    private fun noAnimation(checked: Boolean) {
        mAnimation?.visibility = View.GONE
        mImage?.visibility = View.VISIBLE
        val imageRes = getImageRes(checked)
        mImage?.setImageResource(imageRes)
    }
}