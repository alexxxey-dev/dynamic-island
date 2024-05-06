package com.dynamic.island.oasis.util.ext

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.Vibrator
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.dynamic.island.oasis.BuildConfig
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.Logs
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.s
import java.lang.IllegalStateException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

fun Context.androidId() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
fun String.md5(): String {
    try {

        // Create MD5 Hash
        val digest = MessageDigest
            .getInstance("MD5")
        digest.update(this.toByteArray())
        val messageDigest = digest.digest()

        // Create Hex String
        val hexString = StringBuffer()
        for (i in messageDigest.indices) {
            var h = Integer.toHexString(0xFF and messageDigest[i].toInt())
            while (h.length < 2) h = "0$h"
            hexString.append(h)
        }
        return hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

fun NavController.safePopBackstack(){
    try {
        popBackStack()
    }catch (ex:Exception){
        ex.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(ex)
    }
}

fun Context.analyticsEvent(str: String, bundle: Bundle? = null) {
    FirebaseAnalytics.getInstance(this).logEvent(str, bundle)
}

fun Int.colorToHex(): String {
    return java.lang.String.format("#%06X", 0xFFFFFF and this)
}

fun Activity.statusBarColor(color: Int) {
    try {

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(
            this,
            color
        )
    } catch (ex: Exception) {
        Logs.exception(ex)
    }

}

fun LottieAnimationView?.setLastFrame() {
    if (this == null) return
    this.frame = this.maxFrame.toInt()
}

fun PackageManager.getAppIntent(packageName: String?): Intent? = try {
    if (packageName == null) throw IllegalStateException("package name is null")
    getLaunchIntentForPackage(packageName)
} catch (ex: Exception) {
    Logs.log("get app intent error")
    Logs.exception(ex)
    null
}

fun WakeLock?.destroy() {
    this?.let { if (it.isHeld) it.release() }
}

fun AccessibilityService.createWakeLock(manager: PowerManager): WakeLock? = try {
    val tag = if (Build.MANUFACTURER.lowercase() == "huawei") {
        "LocationManagerService"
    } else {
        packageName
    }


    val wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag)
    wakeLock?.acquire()
    wakeLock
} catch (ex: Exception) {
    ex.printStackTrace()
    Logs.log("create wake lock exception; message = ${ex.message}")
    null
}

fun Window.disableAnimations() {
    try {
        val mLayoutParams = attributes
        val currentFlags =
            mLayoutParams.javaClass.getField("privateFlags").get(mLayoutParams) as Int
        mLayoutParams.javaClass.getField("privateFlags")
            .set(mLayoutParams, currentFlags or 0x00000040)
    } catch (e: java.lang.Exception) {
        //do nothing. Probably using other version of android
    }
}

fun PackageManager.isActivity(event: AccessibilityEvent): Boolean {
    return try {
        val componentName = ComponentName(
            event.packageName.toString(),
            event.className.toString()
        )
        getActivityInfo(componentName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    } != null
}

fun EditText.showKeyboard(
) {
    requestFocus()
    val imm = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as
            InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun EditText.hideKeyboard() {
    val imm = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as
            InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun Long.toDurationString(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(
        TimeUnit.MILLISECONDS.toHours(this)
    )
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(
        TimeUnit.MILLISECONDS.toMinutes(this)
    )
    return if (hours == 0L) {
        String.format("%02d:%02d", minutes, seconds)
    } else {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)

    }
}

fun Fragment.safeNavigate(id: Int, args: Bundle? = null) = try {
    findNavController().navigate(id, args)
} catch (ex: Exception) {
    ex.printStackTrace()
}

fun NavController.safeNavigate(id: Int, args: Bundle? = null) = try {
    navigate(id, args)
} catch (ex: Exception) {
    ex.printStackTrace()
}


fun Context.getAccentColor(packageName: String?): Int? {
    try {
        val pm = packageManager


        val res = pm.getResourcesForApplication(packageName!!)

        val attrs = intArrayOf(
            res.getIdentifier("colorAccent", "attr", packageName),
            android.R.attr.colorAccent
        )

        val theme = res.newTheme()
        val cn = pm.getLaunchIntentForPackage(packageName)?.component
        theme.applyStyle(pm.getActivityInfo(cn!!, 0).theme, false)

        val a = theme.obtainStyledAttributes(attrs)

        val color = a.getColor(0, android.graphics.Color.BLACK)

        a.recycle()
        return color
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }


}

fun Context.getAppTitle(packageName: String?, info: ApplicationInfo? = null): String? = try {
    if (packageName == null) throw Exception("empty package name")
    val mInfo = info ?: packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    packageManager.getApplicationLabel(mInfo).toString()

} catch (ex: Exception) {
    Logs.log("get app title error")
    Logs.exception(ex)
    null
}

fun PackageManager.getAppLogo(packageName: String?): Drawable? = try {
    if (packageName.isNullOrBlank()) throw Exception("empty package name")
    //TODO
    getApplicationIcon(packageName)
} catch (ex: Exception) {
    Logs.log("get app logo error")
    Logs.exception(ex)
    null
}

fun Context.showUrl(url: String) {
    safeStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

fun View.setMargins(top: Int? = null, bottom: Int? = null, right: Int? = null, left: Int? = null) {
    if (this.layoutParams is ViewGroup.MarginLayoutParams) {
        val params = this.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(
            left ?: params.marginStart,
            top ?: params.topMargin,
            bottom ?: params.bottomMargin,
            right ?: params.rightMargin
        )
    }
}


fun Context.destroyReceiver(receiver: BroadcastReceiver?) {
    try {
        unregisterReceiver(receiver)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun CoroutineScope.safeLaunch(block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch {
        try {
            block()
        } catch (ce: CancellationException) {
            // You can ignore or log this exception
            ce.printStackTrace()
        } catch (e: Exception) {
            // Here it's better to at least log the exception
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(Exception(e))
        }
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.createReceiver(receiver: BroadcastReceiver?, filter: IntentFilter): Intent? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }

}

fun Context.launchEmail() {
    val deviceInfo = "Android SDK: ${Build.VERSION.SDK_INT}\n" +
            "Device: ${getDeviceName()}\n" +
            "App Version: ${BuildConfig.VERSION_CODE}\n\n\n"

    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.SUPPORT_EMAIL))
    intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
    intent.putExtra(Intent.EXTRA_TEXT, deviceInfo)
    safeStartActivity(
        Intent.createChooser(
            intent,
            resources.getString(R.string.choose)
        )
    )
}

 fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
    val model = Build.MODEL.lowercase(Locale.getDefault())
    return if (model.startsWith(manufacturer)) {
        model.capitalize(Locale.US)
    } else {
        "$manufacturer $model".capitalize(Locale.US)
    }
}

fun Context.launchMarket() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            )
        )
    } catch (ex: Exception) {
        safeStartActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}

fun Context.shareApp() {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
        putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=$packageName\n\n"
        )
    }
    safeStartActivity(
        Intent.createChooser(
            shareIntent,
            resources.getString(R.string.choose)
        )
    )
}

fun Context.safeStartActivity(intent: Intent?) {
    try {
        startActivity(intent)
    } catch (ex: Exception) {
        Logs.log("startActivityException")
        Logs.exception(ex)
    }
}


fun Vibrator.doVibration(pattern: LongArray) {
    try {

        vibrate(pattern, -1)

    } catch (ex: Exception) {
        ex.printStackTrace()
    }

}

fun BluetoothDevice.getMetadata(): String {
    return try {
        val result = (this.javaClass.getMethod("getMetadata")).invoke(this) as ByteArray
        String(result)
    } catch (ex: Exception) {
        ex.printStackTrace()
        ""
    }
}

fun BluetoothDevice.getBatteryLevel(context: Context): Int? {
    return try {
        val result0 = (this.javaClass.getMethod("getBatteryLevel")).invoke(this) as Int?
        if (result0 != null && result0 > 0) return result0

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val result1 = context.createReceiver(null, filter)?.getIntExtra("level", -1)
        return if (result1 != null && result1 > 0) result1 else null

    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }

}

