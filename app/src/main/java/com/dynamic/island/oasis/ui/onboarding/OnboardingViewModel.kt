package com.dynamic.island.oasis.ui.onboarding

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.launch

class OnboardingViewModel(private val permissions: PermissionsUtil, private val prefs: PrefsUtil) :
    ViewModel() {

    val position = MutableLiveData<Int>()
    val setPosition = SingleLiveEvent<Pair<Int, Boolean>>()
    val skipEnabled = MutableLiveData(true)


    val showDestination = SingleLiveEvent<Int>()

    val accessibility = MutableLiveData<Boolean>()
    val notifications = MutableLiveData<Boolean>()
    val battery = MutableLiveData<Boolean>()
    val phone = MutableLiveData<Boolean>()
    val background = MutableLiveData<Boolean>()
    val autostart = MutableLiveData<Boolean>()

    val autostartAvailable = MutableLiveData<Boolean>()
    val backgroundAvailable = MutableLiveData<Boolean>()

    val showToast = SingleLiveEvent<Int>()


    init {
        checkPermissions()
    }
    fun showPolicyDialog() {
        if (!prefs.policyAccepted()) {
            showDestination.value = R.id.action_onboardingFragment_to_policyDialog
        }

    }

    fun fragmentsCount() = 6

    fun createFragment(position: Int) = when (position) {
        0 -> OnboardingPageFragment.newInstance(R.layout.fragment_onboarding_1)
        1 -> OnboardingPageFragment.newInstance(R.layout.fragment_onboarding_2)
        2 -> OnboardingPageFragment.newInstance(R.layout.fragment_onboarding_3)
        3 -> OnboardingPageFragment.newInstance(R.layout.fragment_onboarding_4)
        4 -> OnboardingPageFragment.newInstance(R.layout.fragment_onboarding_5)
        else -> OnboardingPermissionsFragment()
    }


    fun onAcsbClicked(view: View) {
        if (accessibility.value == true) return
        viewModelScope.safeLaunch {
            permissions.grant(PermissionType.ACSB)
        }


    }

    fun onBackgroundClicked(view:View){
        if(background.value == true) return
        viewModelScope.safeLaunch {
            permissions.grant(PermissionType.START_BACKGROUND)
        }
    }

    fun onAutostartClicked(view:View){
        viewModelScope.safeLaunch {
            permissions.grant(PermissionType.AUTOSTART)
        }
    }

    fun onNotificationsClicked(view: View) {
        if (notifications.value == true) return
        viewModelScope.safeLaunch {
            permissions.grant(PermissionType.NOTIF)
        }

    }

    fun onBatteryClicked(view: View) {
        if (battery.value == true) return
        viewModelScope.safeLaunch {
            permissions.grant(PermissionType.BATTERY)
        }

    }


    fun onPhoneClicked(activity: Activity) {
        if (phone.value == true) return
        viewModelScope.launch {
            permissions.grant(PermissionType.PHONE, activity)
        }

    }


    fun checkPermissions() {
        accessibility.value = permissions.isGranted(PermissionType.ACSB)
        notifications.value = permissions.isGranted(PermissionType.NOTIF)
        battery.value = permissions.isGranted(PermissionType.BATTERY)
        phone.value = permissions.isGranted(PermissionType.PHONE)
        background.value = permissions.isGranted(PermissionType.START_BACKGROUND)
        autostart.value = permissions.isGranted(PermissionType.AUTOSTART)

        backgroundAvailable.value = permissions.isAvailable(PermissionType.START_BACKGROUND)
        autostartAvailable.value = permissions.isAvailable(PermissionType.AUTOSTART)
    }


    fun setSkipEnabled(position: Int) {
        skipEnabled.value = position != (fragmentsCount() - 1)
    }

    fun skip(view: View) {
        setPosition.value = Pair((fragmentsCount() - 2), false)
    }

    fun next(view: View) {
        val mPosition = position.value ?: 0
        if (mPosition == (fragmentsCount() - 1)) {
            prefs.showOnboarding(false)
            showDestination.value = R.id.action_onboardingFragment_to_homeFragment
            return
        }

        setPosition.value = Pair(mPosition + 1, true)
    }
}