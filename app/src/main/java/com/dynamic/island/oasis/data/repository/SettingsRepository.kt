package com.dynamic.island.oasis.data.repository

import android.os.Build
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.AppDatabase
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.MySetting
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsRepository(
    private val db: AppDatabase,
    private val prefs: PrefsUtil
) {

    suspend fun setEnabled(setting: MySetting, enabled:Boolean) = withContext(Dispatchers.IO) {
        prefs.setSettingEnabled(setting.id, enabled)
    }


    fun onSubscriptionInactive() {
        CoroutineScope(Dispatchers.IO).safeLaunch {
            if(prefs.subscription()) return@safeLaunch

            loadSettings().forEach {
                if (it.isPremium) setEnabled(it,false)
            }
            prefs.backgroundColor(prefs.defaultBackgroundColor())
        }
    }



     fun loadSettings(): List<MySetting> = settingsList().sortedBy { it.id }


    private fun settingsList(): List<MySetting> {
        val settings = listOf(
            MySetting(
                id = Constants.SET_CLICK_TO_OPEN,
                text = R.string.open_app_by_click,
                isPremium = false,
            ),
            MySetting(
                id = Constants.SET_DISABLE_LANDSCAPE,
                text = R.string.disable_landscape,
                isPremium = true,
            ),
            MySetting(
                id = Constants.SET_SHOW_ALERT,
                text = R.string.show_alert,
                isPremium = false,
            ),
            MySetting(
                id = Constants.SET_SWIPE_TO_SKIP,
                text = R.string.swipe_to_skip,
                isPremium = true,
            ),
            MySetting(
                id = Constants.SET_QUICK_ACTION,
                text = R.string.quick_action_panel,
                isPremium = false,
            ),
            MySetting(
                id = Constants.SET_BACKGROUND_COLOR,
                text = R.string.oasis_color,
                isPremium = true,
                isColor = true,
            ),
            MySetting(
                id = Constants.SET_NOTIFICATION_ACTIONS,
                text = R.string.oasis_notif_actions,
                isPremium = true,
            ),
            MySetting(
                id = Constants.SET_LOCK_SCREEN,
                text = R.string.oasis_lockscreen,
                isPremium = true,
            ),
            MySetting(
                id = Constants.SET_DISABLE_NOTIFICATIONS,
                text = R.string.disable_notifications,
                isPremium = true,
            )
        ).toMutableList()

        return settings

    }

}