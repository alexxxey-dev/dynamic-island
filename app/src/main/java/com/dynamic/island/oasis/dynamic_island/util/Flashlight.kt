package com.dynamic.island.oasis.dynamic_island.util

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import com.dynamic.island.oasis.dynamic_island.Logs
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Flashlight(private val context:Context, private val cameraManager:CameraManager) {

     fun hasFlashlight() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)



    private suspend fun findFlashlight()= suspendCancellableCoroutine{
        if (!hasFlashlight()) {
           if(it.isActive) it.resume(null)
            return@suspendCancellableCoroutine
        }
        val callback = object:TorchCallback(){
            override fun onTorchModeUnavailable(cameraId: String) {
                super.onTorchModeUnavailable(cameraId)
                cameraManager.unregisterTorchCallback(this)
                if(it.isActive) it.resume(null)
            }

            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                cameraManager.unregisterTorchCallback(this)
                if(it.isActive) it.resume(Pair(cameraId,enabled))
            }
        }
        try {
            cameraManager.registerTorchCallback(callback, null)
        } catch (ex:Exception){
            Logs.log("findFlashlight exception")
            Logs.exception(ex)
            if(it.isActive) it.resume(null)
        }

    }

    suspend fun toggleFlashlight() :Boolean {
        if (!hasFlashlight())   return false
        val flashlight = findFlashlight() ?: return false
        try {
            cameraManager.setTorchMode(flashlight.first, !flashlight.second)
            return true
        }catch (ex:Exception){
            Logs.log("toggleFlashlight exception")
            Logs.exception(ex)
            return false
        }

    }



}