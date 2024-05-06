package com.dynamic.island.oasis.dynamic_island

import android.accessibilityservice.AccessibilityService
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
import com.dynamic.island.oasis.dynamic_island.util.DiParamsProvider
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.store.ListenerStore
import com.dynamic.island.oasis.dynamic_island.store.ViewModelStore
import com.dynamic.island.oasis.dynamic_island.store.ViewStore
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.util.BatteryUtil
import com.dynamic.island.oasis.dynamic_island.util.Flashlight
import com.dynamic.island.oasis.dynamic_island.util.PhoneUtil
import com.dynamic.island.oasis.util.ext.createWakeLock
import com.dynamic.island.oasis.util.ext.destroy
import com.google.firebase.FirebaseApp
import com.google.gson.Gson


class AcsbService : AccessibilityService() {
    var isVisible: Boolean = false
        private set
    private var wakeLock: PowerManager.WakeLock? = null

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
    private var acsbManager:AccessibilityManager? = null

    override fun onServiceConnected() {
        Logs.acsb("acsbService; onServiceConnected")
        super.onServiceConnected()
        setTheme(R.style.Theme_DynamicIsland)

        clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        acsbManager = applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        appOpsManager = applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        power = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = createWakeLock(power!!)
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

        permissions = PermissionsUtil(this, prefsUtil!!, appOpsManager!!,acsbManager!!,power!!,notifications!!)
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
    }


    override fun onUnbind(intent: Intent?): Boolean {
        Logs.acsb("acsbService; onDestroy")

        hideViews()
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
        wakeLock?.destroy()
        acsbManager = null


        return super.onUnbind(intent)
    }



    fun showViews() {
        Logs.acsb("acsbService; showViews")

        if (viewStore != null || isVisible) {
            hideViews()
        }

        viewStore = ViewStore(
            this,
            viewModelStore!!,
            window!!,
            inflater!!
        )
        isVisible = true
    }


    fun hideViews() {
        Logs.acsb("acsbService; hideViews")
        isVisible = false
        viewStore?.onDestroy()
        viewStore = null
    }


    override fun onCreate() {
        Logs.acsb("acsbService; onCreate")
        FirebaseApp.initializeApp(this)
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logs.acsb("acsbService; onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onInterrupt() {
        Logs.acsb("onInterrupt")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        listenerStore?.onAccessibilityEvent(event)
    }


}