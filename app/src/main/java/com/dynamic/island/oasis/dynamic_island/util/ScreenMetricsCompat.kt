package com.dynamic.island.oasis.dynamic_island.util

import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi

object ScreenMetricsCompat {
    private val api: Api =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ApiLevel30()
        else Api()


    fun screenSize(window:WindowManager): Size = api.screenSize(window)

    @Suppress("DEPRECATION")
    private open class Api {
        open fun screenSize(window: WindowManager): Size {
            val display =window.defaultDisplay
            val metrics = if (display != null) {
                DisplayMetrics().also { display.getRealMetrics(it) }
            } else {
                Resources.getSystem().displayMetrics
            }
            return Size(metrics.widthPixels, metrics.heightPixels)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private class ApiLevel30 : Api() {
        override fun screenSize(window: WindowManager): Size {
            val metrics: WindowMetrics = window.currentWindowMetrics
            return Size(metrics.bounds.width(), metrics.bounds.height())
        }
    }
}