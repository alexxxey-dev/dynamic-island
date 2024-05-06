package com.dynamic.island.oasis.util.ext

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.view.animation.Animation.AnimationListener
import com.airbnb.lottie.LottieAnimationView
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@SuppressLint("ClickableViewAccessibility")
fun View?.scaleClickListener(onClick: (v:View) -> Unit) {
    if (this == null) return
    val duration = 50L
    val coef = 1.25f
    val interpolator = DecelerateInterpolator()
    val v = this

    val secondListener =object:AnimationListener{
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            onClick(v)
        }
    }
    val firstListener = object:AnimationListener{
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {

            v.startAnimation(ScaleAnimation(
                coef,1f,
                coef,1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                this.interpolator = interpolator
                this.duration = duration
                setAnimationListener(secondListener)
            })

        }


    }

    setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.clearAnimation()
                v.startAnimation(ScaleAnimation(
                    1f, coef,
                    1f, coef,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    this.interpolator = interpolator
                    this.duration = duration * 2
                    setAnimationListener(firstListener)
                })
            }
        }

        return@setOnTouchListener false
    }

}


suspend fun LottieAnimationView?.play() = suspendCancellableCoroutine<Unit> {
    if (this == null) return@suspendCancellableCoroutine
    this.playAnimation()
    this.addAnimatorListener(object : AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
            if(it.isActive){
                it.resume(Unit)
            }

        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}

    })
}

fun View?.shake() {
    if (this == null) return
    val shake = AnimationUtils.loadAnimation(this.context, R.anim.shake)
    this.startAnimation(shake)
}

suspend fun View?.scaleZero(){
    this?.scaleX = 0f
    this?.scaleY = 0f
}
suspend fun View?.expand(
    startY:Float = 0f,
    startX:Float= 0f,
    time: Long = Constants.EXPAND_TIME,
    pivotX: Float =0f,
    pivotY: Float=0f,
    mInterpolator:Interpolator = OvershootInterpolator()
) = suspendCancellableCoroutine<Unit> {

    if (this == null) return@suspendCancellableCoroutine

    val listener = object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            if(it.isActive) it.resume(Unit)
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    }

    val animation = ScaleAnimation(
        startX, 1f,
        startY, 1f,
        Animation.RELATIVE_TO_SELF, pivotX,
        Animation.RELATIVE_TO_SELF, pivotY
    ).apply {
        interpolator = mInterpolator
        duration = time
        fillAfter = true
        setAnimationListener(listener)
    }
    this.startAnimation(animation)
}
fun View?.hideAlpha(time:Long, onCompleted:()->Unit){
    if(this==null) return
    val animation = AlphaAnimation(1f, 0f).apply {
        duration = time
        fillAfter = true
        setAnimationListener(object:AnimationListener{
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                onCompleted()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }
    this.startAnimation(animation)
}
fun View?.showAlpha(time:Long){
    if(this==null) return
    val animation = AlphaAnimation(0f, 1f).apply {
        duration = time
        fillAfter = true
    }
    this.startAnimation(animation)
}
suspend fun View?.showAlphaCoroutine(time:Long)= suspendCancellableCoroutine<Unit> {
    if (this == null) return@suspendCancellableCoroutine
    val listener = object:AnimationListener{
        override fun onAnimationStart(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
            if(it.isActive) it.resume(Unit)
        }

        override fun onAnimationRepeat(animation: Animation?) {

        }
    }
    val animation = AlphaAnimation(0f, 1f).apply {
        duration = time
        fillAfter = true
        setAnimationListener(listener)
    }
    this.startAnimation(animation)
}
suspend fun View?.collapse(
    toX:Float = 0f,
    toY:Float = 0f,
    time: Long = Constants.COLLAPSE_TIME,
    pivotX: Float = 0f,
    pivotY: Float = 0f,
    mInterpolator:Interpolator = AnticipateInterpolator()
) = suspendCancellableCoroutine<Unit> {
    if (this == null) return@suspendCancellableCoroutine
    val listener = object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
           if(it.isActive) it.resume(Unit)
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    }
    val animation = ScaleAnimation(
        1f, toX,
        1f, toY,
        Animation.RELATIVE_TO_SELF, pivotX,
        Animation.RELATIVE_TO_SELF, pivotY
    ).apply {
        interpolator = mInterpolator
        duration = time
        fillAfter = true
        setAnimationListener(listener)
    }
    this.startAnimation(animation)
}