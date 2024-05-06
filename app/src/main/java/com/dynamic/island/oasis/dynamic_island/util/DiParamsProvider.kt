package com.dynamic.island.oasis.dynamic_island.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.WindowManager
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.util.ext.percentToX
import com.dynamic.island.oasis.util.ext.xToPercent
import com.google.gson.Gson


class DiParamsProvider(
    private val prefs:SharedPreferences,
    private val window:WindowManager,
    private val context: Context,
    private val gson:Gson
) {


    fun providePercent(): DiParams {
        val params = provide()
        val percentX = (window.xToPercent(params.x, params.width) * 100).toInt()
        val percentY = (yToPercent(params.y, params.height) * 100).toInt()
        val percentWidth = (widthToPercent(params.width) * 100).toInt()
        val percentHeight = (heightToPercent(params.height) * 100).toInt()
        return DiParams(
            width = percentWidth,
            height = percentHeight,
            x = percentX,
            y = percentY
        )
    }


    fun provide(): DiParams {
        val cachedParams = cachedParams()
        if (cachedParams == null) {
            val newParams = getDefaultParams()
            cachedParams(newParams)
            return newParams
        }
        return cachedParams
    }




    fun update(
        xPercent: Float? = null,
        yPercent: Float? = null,
        heightPercent: Float? = null,
        widthPercent: Float? = null
    ) {
        val newParams = provide()
        if (widthPercent != null) {
            newParams.width = percentToWidth(widthPercent).toInt()
        }
        if (heightPercent != null) {
            newParams.height = percentToHeight(heightPercent).toInt()
        }
        if (xPercent != null) {
            newParams.x = window.percentToX(xPercent, newParams.width.toFloat()).toInt()
        }
        if (yPercent != null) {
            newParams.y = percentToY(yPercent, newParams.height.toFloat()).toInt()
        }
        cachedParams(newParams)
        context.sendBroadcast(Intent(Constants.ACTION_CHANGE_DI_PARAMS))
    }

    private fun cachedParams(value: DiParams? = null): DiParams? {
        if (value != null) {
            val json = gson.toJson(value)
            prefs.edit().putString(Constants.PREFS_DI_PARAMS, json).apply()
        }

        val json = prefs.getString(Constants.PREFS_DI_PARAMS, null) ?: return null

        return gson.fromJson(json, DiParams::class.java)
    }


     private fun getDefaultParams() = DiParams().apply {
        width = percentToWidth(Constants.DEFAULT_WIDTH).toInt()
        height = percentToHeight(Constants.DEFAULT_HEIGHT).toInt()
        x = window.percentToX(Constants.DEFAULT_X, width.toFloat()).toInt()
        y = percentToY(Constants.DEFAULT_Y, height.toFloat()).toInt()
    }

    fun percentToY(pY: Float, height: Float): Float {
        val screenHeight = ScreenMetricsCompat.screenSize(window).height.toFloat()
        return (screenHeight - height / 2) * pY
    }

    fun yToPercent(y: Int, height: Int): Float = try {
        val screenHeight = ScreenMetricsCompat.screenSize(window).height.toFloat()
        y /(screenHeight - height / 2)
    } catch (ex: Exception) {
        Logs.log("yToPercent exception")
        Logs.exception(ex)
        0f
    }


    private fun heightToPercent(height: Int): Float = try {
        val max = maxHeight()
        val min = minHeight()
        (height - min) / (max - min)
    } catch (ex: Exception) {
        Logs.log("heightToPercent exception")
        Logs.exception(ex)
        0f
    }

    private fun percentToHeight(pHeight: Float): Float {
        val max = maxHeight()
        val min = minHeight()
        return (max - min) * pHeight + min
    }

    private fun widthToPercent(width: Int): Float = try {
        val max = maxWidth()
        val min = minWidth()
        (width - min) / (max - min)
    } catch (ex: Exception) {
        Logs.log("widthToPercent exception")
        Logs.exception(ex)
        0f
    }

    private fun percentToWidth(pWidth: Float): Float {
        val max = maxWidth()
        val min = minWidth()
        return (max - min) * pWidth + min
    }


    private fun maxWidth(): Float {
        return context.resources.getDimension(R.dimen.di_max_width)
    }

    private fun minWidth(): Float {
        return context.resources.getDimension(R.dimen.di_min_width)
    }

    private fun minHeight(): Float {
        return context.resources.getDimension(R.dimen.di_min_height)
    }

    private fun maxHeight(): Float {
        return context.resources.getDimension(R.dimen.di_max_height)
    }


}