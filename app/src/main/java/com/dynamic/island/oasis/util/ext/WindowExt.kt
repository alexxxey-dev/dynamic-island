package com.dynamic.island.oasis.util.ext

import android.view.View
import android.view.WindowManager
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.util.ScreenMetricsCompat


fun WindowManager.percentToX(pX: Float, mWidth: Float): Float {
    val screenWidth = ScreenMetricsCompat.screenSize(this).width.toFloat()
    return (screenWidth - mWidth) / 2 * (pX - (1 - pX))
}





fun WindowManager.xToPercent(x: Int?, width: Int?): Float = try {
    if (x == null || width == null) throw Exception()
    val screenWidth = ScreenMetricsCompat.screenSize(this).width.toFloat()
    (2 * x + screenWidth - width) / (2 * screenWidth - 2 * width)
} catch (ex: Exception) {
    0f
}

fun View?.diffYPercent(first: DiParams?): Float = try {
    if (this == null || first == null) throw  Exception()
    val h1 = first.height.toFloat()
    val h2 = this.height.toFloat()
    Math.min(h1, h2) / Math.max(h1, h2)
} catch (ex: Exception) {
    Logs.log("diffYPercent exception")
    Logs.exception(ex)
    0f
}


fun WindowManager.safeUpdateView(view: View?, params: WindowManager.LayoutParams?) {
    try {
        this.updateViewLayout(view, params)
    } catch (ex: Exception) {
    }
}

fun WindowManager.safeAddView(view: View?, params: WindowManager.LayoutParams?) {
    try {
        this.addView(view, params)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun WindowManager.safeRemoveView(view: View?) {
    try {
        this.removeView(view)
    } catch (ex: Exception) {
    }
}



