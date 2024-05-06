package com.dynamic.island.oasis.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.dynamic.island.oasis.dynamic_island.Logs
import java.util.*


class AutoStart(private val context:Context) {

    /***
     * Xiaomi
     */
    private val BRAND_XIAOMI = "xiaomi"
    private val BRAND_XIAOMI_POCO = "poco"
    private val BRAND_XIAOMI_REDMI = "redmi"
    private val PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter"
    private val PACKAGE_XIAOMI_COMPONENT =
        "com.miui.permcenter.autostart.AutoStartManagementActivity"

    /***
     * Letv
     */
    private val BRAND_LETV = "letv"
    private val PACKAGE_LETV_MAIN = "com.letv.android.letvsafe"
    private val PACKAGE_LETV_COMPONENT = "com.letv.android.letvsafe.AutobootManageActivity"

    /***
     * ASUS ROG
     */
    private val BRAND_ASUS = "asus"
    private val PACKAGE_ASUS_MAIN = "com.asus.mobilemanager"
    private val PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings"
    private val PACKAGE_ASUS_COMPONENT_FALLBACK =
        "com.asus.mobilemanager.autostart.AutoStartActivity"

    /***
     * Honor
     */
    private val BRAND_HONOR = "honor"
    private val PACKAGE_HONOR_MAIN = "com.huawei.systemmanager"
    private val PACKAGE_HONOR_COMPONENT =
        "com.huawei.systemmanager.optimize.process.ProtectActivity"

    /***
     * Huawei
     */
    private val BRAND_HUAWEI = "huawei"
    private val PACKAGE_HUAWEI_MAIN = "com.huawei.systemmanager"
    private val PACKAGE_HUAWEI_COMPONENT =
        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
    private val PACKAGE_HUAWEI_COMPONENT_FALLBACK =
        "com.huawei.systemmanager.optimize.process.ProtectActivity"


    /**
     * Vivo
     */

    private val BRAND_VIVO = "vivo"
    private val PACKAGE_VIVO_MAIN = "com.iqoo.secure"
    private val PACKAGE_VIVO_FALLBACK = "com.vivo.permissionmanager"
    private val PACKAGE_VIVO_COMPONENT = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
    private val PACKAGE_VIVO_COMPONENT_FALLBACK =
        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
    private val PACKAGE_VIVO_COMPONENT_FALLBACK_A =
        "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"

    /**
     * Nokia
     */

    private val BRAND_NOKIA = "nokia"
    private val PACKAGE_NOKIA_MAIN = "com.evenwell.powersaving.g3"
    private val PACKAGE_NOKIA_COMPONENT =
        "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity"

    /***
     * Samsung
     */
    private val BRAND_SAMSUNG = "samsung"
    private val PACKAGE_SAMSUNG_MAIN = "com.samsung.android.lool"
    private val PACKAGE_SAMSUNG_COMPONENT = "com.samsung.android.sm.ui.battery.BatteryActivity"
    private val PACKAGE_SAMSUNG_COMPONENT_2 =
        "com.samsung.android.sm.battery.ui.usage.CheckableAppListActivity"
    private val PACKAGE_SAMSUNG_COMPONENT_3 = "com.samsung.android.sm.battery.ui.BatteryActivity"

    /***
     * One plus
     *
     * Intent { act=com.oplus.battery.permission.startup.StartupAppListActivity
     * pkg=com.oplus.battery cmp=com.oplus.battery/com.oplus.startupapp.view.StartupAppListActivity mCallingUid=1000 }
     *
     */

    private val BRAND_ONE_PLUS = "oneplus"

    private val PACKAGE_ONE_PLUS_MAIN = "com.oneplus.security"
    private val PACKAGE_ONE_PLUS_COMPONENT =
        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"

    private val PACKAGE_ONE_PLUS_FALLBACK = "com.oplus.battery"
    private val PACKAGE_ONE_PLUS_COMPONENT_FALLBACK =
        "com.oplus.startupapp.view.StartupAppListActivity"

    private val PACKAGE_ONE_PLUS_ACTION = "com.android.settings.action.BACKGROUND_OPTIMIZE"
    private val PACKAGE_ONE_PLUS_ACTION_FALLBACK =
        "com.oplus.battery.permission.startup.StartupAppListActivity"

    /**
     * Oppo
     */
    private val BRAND_OPPO = "oppo"
    private val PACKAGE_OPPO_MAIN = "com.coloros.safecenter"
    private val PACKAGE_OPPO_FALLBACK = "com.oppo.safe"
    private val PACKAGE_OPPO_COMPONENT =
        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
    private val PACKAGE_OPPO_COMPONENT_FALLBACK =
        "com.oppo.safe.permission.startup.StartupAppListActivity"
    private val PACKAGE_OPPO_COMPONENT_FALLBACK_A =
        "com.coloros.safecenter.startupapp.StartupAppListActivity"


    fun autoStartBruteforce( open: Boolean, newTask: Boolean): Boolean {
        return autoStart(
            context,
            listOf(
                PACKAGE_ASUS_MAIN,
                PACKAGE_XIAOMI_MAIN,
                PACKAGE_LETV_MAIN,
                PACKAGE_HONOR_MAIN,
                PACKAGE_OPPO_MAIN,
                PACKAGE_OPPO_FALLBACK,
                PACKAGE_VIVO_MAIN,
                PACKAGE_VIVO_FALLBACK,
                PACKAGE_NOKIA_MAIN,
                PACKAGE_HUAWEI_MAIN,
                PACKAGE_SAMSUNG_MAIN,
                PACKAGE_ONE_PLUS_MAIN,
                PACKAGE_ONE_PLUS_FALLBACK
            ),
            listOf(
                getIntent(PACKAGE_ONE_PLUS_MAIN, PACKAGE_ONE_PLUS_COMPONENT, newTask),
                getIntent(PACKAGE_ONE_PLUS_FALLBACK, PACKAGE_ONE_PLUS_COMPONENT_FALLBACK, newTask),
                getIntent(PACKAGE_XIAOMI_MAIN, PACKAGE_XIAOMI_COMPONENT, newTask),
                getIntent(PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT, newTask),
                getIntent(PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT_FALLBACK, newTask),
                getIntent(PACKAGE_LETV_MAIN, PACKAGE_LETV_COMPONENT, newTask),
                getIntent(PACKAGE_HONOR_MAIN, PACKAGE_HONOR_COMPONENT, newTask),
                getIntent(PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT, newTask),
                getIntent(PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT_FALLBACK, newTask),
                getIntent(PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT, newTask),
                getIntent(PACKAGE_OPPO_FALLBACK, PACKAGE_OPPO_COMPONENT_FALLBACK, newTask),
                getIntent(PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT_FALLBACK_A, newTask),
                getIntent(PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT, newTask),
                getIntent(PACKAGE_VIVO_FALLBACK, PACKAGE_VIVO_COMPONENT_FALLBACK, newTask),
                getIntent(PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT_FALLBACK_A, newTask),
                getIntent(PACKAGE_NOKIA_MAIN, PACKAGE_NOKIA_COMPONENT, newTask),
                getIntent(PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT, newTask),
                getIntent(PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT_2, newTask),
                getIntent(PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT_3, newTask)
            ),
            open
        ) || autoStartFromAction(
            listOf(getIntentFromAction(PACKAGE_ONE_PLUS_ACTION, newTask)),
            open
        ) || autoStartFromAction(
            listOf(getIntentFromAction(PACKAGE_ONE_PLUS_ACTION_FALLBACK, newTask)),
            open
        ) || launchOppoAppInfo( open, newTask)
    }


    private fun launchOppoAppInfo( open: Boolean, newTask: Boolean): Boolean {
        if (!Build.BRAND.lowercase().startsWith("oppo")) return false
        return try {

            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:${context.packageName}")
            if (open) {
                context.startActivity(i)
                true
            } else {
                isActivityFound( i)
            }
        } catch (exx: Exception) {
            exx.printStackTrace()
            false
        }
    }


    @Throws(Exception::class)
    private fun startIntent( intent: Intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (exception: Exception) {
            exception.printStackTrace()
            throw exception
        }
    }

    private fun isPackageExists(targetPackage: String): Boolean {
        val packages: List<ApplicationInfo>
        val pm = context.packageManager
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName == targetPackage) {
                return true
            }
        }
        return false
    }



    /**
     * Generates an intent with the passed package and component name
     * @param packageName
     * @param componentName
     * @param newTask
     *
     * @return the intent generated
     */
    private  fun getIntent(packageName: String, componentName: String, newTask: Boolean): Intent {
        return Intent().apply {
            component = ComponentName(packageName, componentName)
            if (newTask) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Generates an intent with the passed action
     * @param intentAction
     * @param newTask
     *
     * @return the intent generated
     */
    private fun getIntentFromAction(intentAction: String, newTask: Boolean): Intent {
        return Intent().apply {
            action = intentAction
            if (newTask) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Will query the passed intent to check whether the Activity really exists
     *
     * @param context
     * @param intent, intent to open an activity
     *
     * @return true if activity is found, false otherwise
     */
    private fun isActivityFound(intent: Intent): Boolean {
        return context.packageManager.queryIntentActivities(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        ).isNotEmpty()
    }

    /**
     * Will query the passed list of intents to check whether any of the activities exist
     *
     * @param context
     * @param intents, list of intents to open an activity
     *
     * @return true if activity is found, false otherwise
     */
    private fun areActivitiesFound( intents: List<Intent>): Boolean {
        return intents.any { isActivityFound( it) }
    }

    /**
     * Will attempt to open the AutoStart settings activity from the passed list of intents in order.
     * The first activity found will be opened.
     *
     * @param context
     * @param intents list of intents
     *
     * @return true if an activity was opened, false otherwise
     */
    private fun openAutoStartScreen( intents: List<Intent>): Boolean {
        intents.forEach {
            if (isActivityFound( it)) {
                Logs.log(
                    "activity found; pn=${it.component?.packageName}; cls=${it.component?.className}"
                )
                startIntent( it)
                return@openAutoStartScreen true
            }
        }
        return false
    }



    /**
     * Will trigger the common autostart permission logic. If [open] is true it will attempt to open the specific
     * manufacturer setting screen, otherwise it will just check for its existence
     *
     * @param context
     * @param packages, list of known packages of the corresponding manufacturer
     * @param intents, list of known intents that open the corresponding manufacturer settings screens
     * @param open, if true it will attempt to open the settings screen, otherwise it just check its existence
     * @return true if the screen was opened or exists, false if it doesn't exist or could not be opened
     */
    private fun autoStart(
        context: Context,
        packages: List<String>,
        intents: List<Intent>,
        open: Boolean
    ): Boolean {
        return if (packages.any { isPackageExists(it) }) {
            if (open) {
                openAutoStartScreen( intents)
            } else {
                areActivitiesFound( intents)
            }
        } else false
    }

    /**
     * Will trigger the common autostart permission logic. If [open] is true it will attempt to open the specific
     * manufacturer setting screen, otherwise it will just check for its existence
     *
     * @param context
     * @param intentActions, list of known intent actions that open the corresponding manufacturer settings screens
     * @param open, if true it will attempt to open the settings screen, otherwise it just check its existence
     * @return true if the screen was opened or exists, false if it doesn't exist or could not be opened
     */
    private fun autoStartFromAction(
        intentActions: List<Intent>,
        open: Boolean
    ): Boolean {
        return if (open) openAutoStartScreen( intentActions)
        else areActivitiesFound( intentActions)
    }
}
