package com.dynamic.island.oasis.util

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.text.TextUtils
import java.lang.reflect.Method

class MIUI(private val context:Context, private val mgr: AppOpsManager) {
    private val OP_BACKGROUND_START_ACTIVITY = 10021
    private val OP_AUTO_START = 10008
    fun isMIUI(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    private fun hasPermissionMIUI(permission: Int): Boolean {
        try {

            val m: Method = AppOpsManager::class.java.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val result = m.invoke(
                mgr,
                permission,
                Process.myUid(),
                context.applicationContext.packageName
            ) as Int
            return result == AppOpsManager.MODE_ALLOWED
        } catch (x: java.lang.Exception) {

        }
        return true
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(key: String?): String? {
        try {
            val props = Class.forName("android.os.SystemProperties")
            return props.getMethod("get", String::class.java).invoke(null, key) as String
        } catch (ignore: java.lang.Exception) {
        }
        return null
    }

    fun autostartGranted() = hasPermissionMIUI(OP_AUTO_START)
    fun startFromBackgroundGranted() = hasPermissionMIUI(OP_BACKGROUND_START_ACTIVITY)
}