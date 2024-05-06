package com.dynamic.island.oasis.dynamic_island.ui.features.screenshot

import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutAppScreenshotBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.AppData
import com.dynamic.island.oasis.dynamic_island.data.DiBackground
import com.dynamic.island.oasis.util.ext.safeAddView
import com.dynamic.island.oasis.util.ext.safeRemoveView

class ScreenshotView(
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) {

    private var binding: LayoutAppScreenshotBinding? = null
    private val params = WindowManager.LayoutParams().apply {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.TRANSLUCENT
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
    }




    fun hide() {
        Logs.view("hide_screenshot_view")
        windowManager.safeRemoveView(binding?.root)
        binding = null
    }

    fun show(appData: AppData) {
        Logs.view("show_screenshot_view")
        binding = DataBindingUtil.inflate(
            inflater, R.layout.layout_app_screenshot,
            null, false
        )
        windowManager.safeAddView(binding?.root, params)
        setupView(appData)
    }

    fun setupView(it:AppData){
        try {
            Logs.log("updateScreenshotBackground")
            binding?.appLogo?.setImageDrawable(it.logo)
            binding?.appTitle?.text = it.title
            binding?.background?.setCardBackgroundColor(it.diBackground.color)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

}