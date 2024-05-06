package com.dynamic.island.oasis.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.BillingUtil
import com.dynamic.island.oasis.data.MyConfig
import com.dynamic.island.oasis.data.models.MySetting
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.data.repository.SettingsRepository
import com.dynamic.island.oasis.ui.animated_switch.AnimatedSwitch
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val prefs: PrefsUtil,
    private val repository: SettingsRepository,
    private val permissions: PermissionsUtil,
    private val config: MyConfig,
    private val billing: BillingUtil
) :
    ViewModel() {
        val updateList = SingleLiveEvent<Unit>()
    val showDestinationBundle = SingleLiveEvent<Pair<Int, Bundle>>()
    val subscription = MutableLiveData<Boolean>()
    val showDestination = SingleLiveEvent<Int>()
    val settingsList = MutableLiveData<List<MySetting>>()
    val sendBroadcast = SingleLiveEvent<Intent>()
    val updateItem = SingleLiveEvent<MySetting>()
    val showPaywall = SingleLiveEvent<Unit>()
    val showToast = SingleLiveEvent<Int>()
     fun init() = viewModelScope.safeLaunch{
        settingsList.value = repository.loadSettings()
    }

    fun loadSubscription(){
        subscription.value = prefs.subscription()
    }
    fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.ACTION_UPDATE_BG) {
            updateBackgroundColor()
        } else if (intent.action == Constants.ACTION_SUBSCRIPTION_ACTIVATED || intent.action == Constants.ACTION_SUBSCRIPTION_DEACTIVATED){
            loadSubscription()
            updateList.value = Unit
        }
    }

    fun isEnabled(setting: MySetting) = prefs.settingEnabled(setting.id)

    private fun updateBackgroundColor() {
        viewModelScope.safeLaunch {
            val mSettings = settingsList.value ?: emptyList()
            val colorSetting = mSettings.filter { it.isColor }
            colorSetting.forEach { updateItem.value = it }

        }
    }

    fun subscription() = prefs.subscription()

    fun backgroundColor() = prefs.backgroundColor()


    fun onPermissionsClicked(view: View) {
        showDestination.value = R.id.action_settingsFragment_to_permissionsFragment
    }


    fun onSwitchClicked( switch: AnimatedSwitch, item:MySetting){
        viewModelScope.safeLaunch {
            val enabled = subscription() || !item.isPremium
            if(!enabled){
                   showPaywall.value = Unit
                return@safeLaunch
            }

            switch.onClicked()
        }

    }

    fun onSettingToggle(activity: Activity, setting: MySetting, checked: Boolean) {
        viewModelScope.safeLaunch {
            if(setting.id == Constants.SET_NOTIFICATION_ACTIONS){
                sendBroadcast.value = Intent(Constants.ACTION_NOTIFICATION_ACTIONS_UPDATED)
            }
            activity.analyticsEvent("on_setting_toggle",bundleOf(

                "enabled" to checked,
                "id" to setting.id
            ))

            repository.setEnabled(setting, checked)
        }
    }


    fun showColorPicker(item:MySetting) {
        viewModelScope.safeLaunch {
            val enabled = subscription() || !item.isPremium
            if(!enabled){
                showPaywall.value = Unit
                return@safeLaunch
            }
            showDestination.value = R.id.action_settingsFragment_to_colorPickerDialog
        }


    }

    fun onAppsClicked(view: View) {
        showDestination.value = R.id.action_settingsFragment_to_appsFragment
    }
}