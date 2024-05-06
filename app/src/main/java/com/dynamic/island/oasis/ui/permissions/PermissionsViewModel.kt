package com.dynamic.island.oasis.ui.permissions

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.MyPermission
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.data.repository.PermissionsRepository
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.safeLaunch

class PermissionsViewModel(
    private val repository: PermissionsRepository,
    private val permissionsUtil: PermissionsUtil,
    private val prefs: PrefsUtil
):ViewModel() {
    val permissions = MutableLiveData<List<MyPermission>>()
    val showToast = SingleLiveEvent<Int>()
    val updateItem = SingleLiveEvent<MyPermission>()
    val subscription = MutableLiveData<Boolean>()

    fun loadSubscription(){
        subscription.value = prefs.subscription()
    }


     fun init() = viewModelScope.safeLaunch{
        permissions.value = repository.loadPermissions()
    }

    fun updatePermissions(){
        viewModelScope.safeLaunch {
            permissions.value?.forEach {
                updateItem.value =it
            }
        }
    }




    fun isGranted(permission: MyPermission) = permissionsUtil.isGranted(permission.type)


    fun grantPermission(permission: MyPermission, activity: Activity){
        if(permissionsUtil.isGranted(permission.type) && permission.type != PermissionType.AUTOSTART) return

        viewModelScope.safeLaunch {
            permissionsUtil.grant(permission.type,activity)
        }
    }
}