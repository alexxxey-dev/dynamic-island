package com.dynamic.island.oasis.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.dynamic_island.service.MainService
import com.dynamic.island.oasis.dynamic_island.listeners.notifications.NotificationListener
import com.dynamic.island.oasis.util.ext.safeStartActivity
import kotlinx.coroutines.delay
import java.lang.IllegalStateException

class PermissionsUtil(
    private val context: Context,
    private val prefs: PrefsUtil,
    private val appOpsManager: AppOpsManager,
    private val powerManager: PowerManager,
    private val notificationManager: NotificationManager
) {
    private val miui = MIUI(context, appOpsManager)
    private val autoStart = AutoStart(context)

    fun grant(type: PermissionType, activity: Activity? = null) {
        when (type) {
            PermissionType.PHONE -> requestPhonePermission(activity)
            PermissionType.DRAW_OVERLAY -> requestOverlay()
            PermissionType.BATTERY -> requestIgnoringBattery()
            PermissionType.NOTIF -> requestNotificationListener()
            PermissionType.START_BACKGROUND -> requestStartFromBackgroundMIUI()
            PermissionType.AUTOSTART -> requestAutostart()
        }
    }


    fun isGranted(type: PermissionType): Boolean {
        return when (type) {
            PermissionType.PHONE -> hasPhonePermission()
            PermissionType.DRAW_OVERLAY -> isOverlay()
            PermissionType.BATTERY -> isIgnoringBattery()
            PermissionType.NOTIF -> notificationListener()
            PermissionType.START_BACKGROUND -> if(miui.isMIUI()) miui.startFromBackgroundGranted() else true
            PermissionType.AUTOSTART -> if (miui.isMIUI()) miui.autostartGranted() else prefs.autostart()
        }
    }

    fun isAvailable(type: PermissionType): Boolean {
        return when (type) {
            PermissionType.START_BACKGROUND -> miui.isMIUI()
            PermissionType.AUTOSTART -> autoStart.autoStartBruteforce(open = false, newTask = false)
            else -> true
        }
    }


    private fun requestOverlay() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            try {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Toast.makeText(context, R.string.overlay_toast, Toast.LENGTH_LONG).show()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun isOverlay() = Settings.canDrawOverlays(context)


    private fun requestAutostart() {
        val success = try {
            autoStart.autoStartBruteforce(open = true, newTask = false)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Toast.makeText(
                    context.applicationContext,
                    R.string.autostart_toast,
                    Toast.LENGTH_LONG
                ).show()
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }


        }
        if (success) prefs.autostart(true)
    }

    private fun requestStartFromBackgroundMIUI() {
        try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                putExtra("extra_pkgname", context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            Toast.makeText(context.applicationContext, R.string.start_from_background_toast, Toast.LENGTH_SHORT).show()
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private fun isIgnoringBattery() =
        (powerManager).isIgnoringBatteryOptimizations(context.packageName)


    private fun hasPhonePermission() = hasPermission(android.Manifest.permission.READ_PHONE_STATE)
            && hasPermission(android.Manifest.permission.READ_CONTACTS)
            && hasPermission(android.Manifest.permission.CALL_PHONE)

    private fun requestPhonePermission(activity: Activity? = null) {
        if (activity == null) throw IllegalStateException("Activity is null, cant request phone permission")
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.CALL_PHONE
            ), Constants.CODE_PHONE_PERMISSION
        )
    }


    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestIgnoringBattery() {
        try {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun notificationListener(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val component = ComponentName(context, NotificationListener::class.java)

            return notificationManager.isNotificationListenerAccessGranted(component)
        }


        val list = NotificationManagerCompat.getEnabledListenerPackages(context)
        return list.contains(context.packageName)
    }



    private fun requestNotificationListener() {
        Toast.makeText(context.applicationContext, R.string.notifications_toast, Toast.LENGTH_SHORT).show()
        context.safeStartActivity(
            Intent(Constants.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }
}