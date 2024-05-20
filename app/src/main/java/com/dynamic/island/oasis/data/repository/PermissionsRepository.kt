package com.dynamic.island.oasis.data.repository

import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.models.MyPermission
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.util.PermissionsUtil

class PermissionsRepository(
    private val permissions: PermissionsUtil
) {






    fun loadPermissions() :List<MyPermission>{
        val mList = mutableListOf(
            MyPermission(
                id = 0,
                text = R.string.draw_overlay,
                type = PermissionType.DRAW_OVERLAY
            ),
            MyPermission(
                id = 1,
                text = R.string.notifications,
                type = PermissionType.NOTIF
            ),
            MyPermission(
                id = 2,
                text = R.string.phone_contacts,
                type = PermissionType.PHONE
            ),
            MyPermission(
                id = 4,
                text = R.string.battery_optimization,
                type = PermissionType.BATTERY
            )
        )
        if(permissions.isAvailable(PermissionType.START_BACKGROUND)){
            mList.add(
                MyPermission(
                    id = 5,
                    text = R.string.start_from_background,
                    type = PermissionType.START_BACKGROUND
                )
            )
        }
        if(permissions.isAvailable(PermissionType.AUTOSTART)){
            mList.add(
                MyPermission(
                    id=6,
                    text=R.string.autostart,
                    type= PermissionType.AUTOSTART
                )
            )
        }
        return mList
    }
}