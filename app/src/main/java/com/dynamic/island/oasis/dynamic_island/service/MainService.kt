package com.dynamic.island.oasis.dynamic_island.service

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.camera2.CameraManager
import android.media.session.MediaSessionManager
import android.os.PowerManager
import android.os.Vibrator
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.dynamic.island.oasis.dynamic_island.util.DiParamsProvider
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.store.ListenerStore
import com.dynamic.island.oasis.dynamic_island.store.ViewModelStore
import com.dynamic.island.oasis.dynamic_island.store.ViewStore
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.listeners.restart.RestartWorker
import com.dynamic.island.oasis.dynamic_island.util.BatteryUtil
import com.dynamic.island.oasis.dynamic_island.util.Flashlight
import com.dynamic.island.oasis.dynamic_island.util.PhoneUtil
import com.dynamic.island.oasis.util.ext.createWakeLock
import com.dynamic.island.oasis.util.ext.destroy
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import java.util.concurrent.TimeUnit


class MainService : ServiceWrapper() {
    private var listenerStore: ListenerStore? = null
    private var viewModelStore: ViewModelStore? = null
    private var viewStore: ViewStore? = null

    private var permissions: PermissionsUtil? = null
    private var prefsUtil: PrefsUtil? = null
    private var diParams: DiParamsProvider? = null
    private var gson: Gson? = null
    private var prefs:SharedPreferences? = null
    private var window:WindowManager? = null
    private var battery:BatteryUtil? = null
    private var vibrator:Vibrator? = null
    private var notifications:NotificationManager? = null
    private var phone:PhoneUtil? = null
    private var telecom:TelecomManager? = null
    private var flashlight:Flashlight? = null
    private var camera:CameraManager? = null
    private var keyguard:KeyguardManager? = null
    private var media:MediaSessionManager? = null
    private var telephony:TelephonyManager? = null
    private var clipboard:ClipboardManager? = null
    private var inflater:LayoutInflater? = null
    private var power:PowerManager? = null
    private var appOpsManager:AppOpsManager? = null

    override fun onStart() {
        Logs.acsb("acsbService; onServiceConnected")
        FirebaseApp.initializeApp(this)
        setTheme(R.style.Theme_DynamicIsland)

        clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        appOpsManager = applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        power = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        gson = Gson()
        inflater = LayoutInflater.from(this )
        telephony = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        media = applicationContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        keyguard = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        camera = applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        flashlight = Flashlight(this,camera!! )
        telecom = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        phone = PhoneUtil(telecom!!,contentResolver,resources)
        notifications = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        battery = BatteryUtil(this)
        window = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefs= applicationContext.getSharedPreferences(packageName, Context.MODE_PRIVATE)
        prefsUtil = PrefsUtil(this, prefs!!)
        diParams = DiParamsProvider(prefs!!,window!!,this,gson!!)

        permissions = PermissionsUtil(this, prefsUtil!!, appOpsManager!!,power!!,notifications!!)
        viewModelStore = ViewModelStore(
            this,
            prefsUtil!!,
            permissions!!,
            diParams!!,
            battery!!,
            gson!!,
            vibrator!!,
            notifications!!,
            phone!!,
            flashlight!!,
            keyguard!!
        )
        listenerStore = ListenerStore(
            this,
            permissions!!,
            viewModelStore!!,
            media!!,
            telephony!!,
            clipboard!!
        )
        viewStore = ViewStore(
            this,
            viewModelStore!!,
            window!!,
            inflater!!
        )
    }


    override fun onStop() {
        Logs.acsb("acsbService; onDestroy")

        viewStore?.onDestroy()
        viewStore = null
        viewModelStore?.onDestroy()
        listenerStore?.onDestroy()

        gson = null
        inflater = null
        telephony = null
        media = null
        keyguard = null
        camera = null
        flashlight = null
        telecom = null
        phone = null
        notifications = null
        vibrator = null
        battery = null
        window = null
        prefs = null
        prefsUtil =null
        diParams = null
        permissions = null
        power = null
        viewModelStore = null
        listenerStore = null
        appOpsManager = null
        clipboard = null
    }







    companion object{
        fun init(context: Context) {
            val request = PeriodicWorkRequest.Builder(
                RestartWorker::class.java, RestartWorker.RESTART_PERIOD_MIN, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                RestartWorker.RESTART_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MainService::class.java))
        }

        fun startViaWorker(context: Context) {
            val prefs= context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            val prefsUtil = PrefsUtil(context, prefs)
            if (!prefsUtil.serviceEnabled()) return

            val request = OneTimeWorkRequest.Builder(RestartWorker::class.java).build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun isRunning(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val serviceClass = MainService::class.java
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }
    }

}