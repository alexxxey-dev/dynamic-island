package com.dynamic.island.oasis.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.dynamic.island.oasis.BuildConfig
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R

class PrefsUtil(private val context: Context, private val prefs: SharedPreferences) {
    fun serviceEnabled(enabled: Boolean? = null) = bool("service_enabled",enabled, default = false)

    fun autostart(enabled: Boolean? = null) = bool("autostart", enabled, default = false)


    fun showLockDialog(value: Boolean? = null) = false

    fun appCompatible(packageName: String?): Boolean {
        if (packageName == null) return true
        return bool("app_compatible_$packageName", default = true)
    }

    fun setCompatible(packageName: String, compatible: Boolean) =
        bool("app_compatible_$packageName", compatible)

    fun setSettingEnabled(id: Int, enabled: Boolean) = bool("setting_enabled_$id", enabled)


    fun settingEnabled(id: Int) = bool("setting_enabled_$id", default = true)

    fun rateCount(value: Int? = null) = int(Constants.PREFS_SCREEN_OPENS, value)

    fun interCount(value: Int? = null) = int(Constants.PREFS_INTER_COUNT, value)


    fun lastAppsUpdate(value: Long? = null) = long(Constants.PREFS_LAST_APPS_UPDATE, value)


    fun subscription(value: Boolean? = null): Boolean {
        //TODO
        return  if(BuildConfig.DEBUG) true else bool(Constants.PREFS_SUBSCRIPTION, value)
        //return  bool(Constants.PREFS_SUBSCRIPTION, value)
    }


    fun showOnboarding(value: Boolean? = null) = bool(Constants.PREFS_SHOW_ONBOARDING, value, true)


    fun policyAccepted(value: Boolean? = null) = bool(Constants.PREFS_FIRST_LAUNCH, value, false)


    fun isBackgroundNotch(value: Boolean? = null) = bool(Constants.PREFS_NOTCH, value)

    fun backgroundColor(value: Int? = null) =
        int(Constants.PREFS_BACKGROUND_COLOR, value, defaultBackgroundColor())


    fun defaultBackgroundColor() = ContextCompat.getColor(context, R.color.black)

    private fun bool(key: String, value: Boolean? = null, default: Boolean = false): Boolean {
        if (value != null) {
            prefs.edit().putBoolean(key, value).apply()
        }

        return prefs.getBoolean(key, default)
    }

    private fun string(key: String, value: String? = null, defaultValue: String? = null): String? {
        if (value != null) {
            prefs.edit().putString(key, value).apply()
        }
        return prefs.getString(key, defaultValue)
    }

    private fun long(key: String, value: Long? = null): Long {
        if (value != null) {
            prefs.edit().putLong(key, value).apply()
        }
        return prefs.getLong(key, 0L)
    }

    private fun float(key: String, value: Float? = null, default: Float = 0.0f): Float {
        if (value != null) {
            prefs.edit().putFloat(key, value).apply()
        }

        return prefs.getFloat(key, default)
    }

    private fun int(key: String, value: Int? = null, default: Int = 0): Int {
        if (value != null) {
            prefs.edit().putInt(key, value).apply()
        }

        return prefs.getInt(key, default)
    }

}