package com.dynamic.island.oasis.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey



data class MyPermission(
    val id:Int,
    val text:Int,
    val type: PermissionType
){

}

enum class PermissionType{
    NOTIF,PHONE,BATTERY, START_BACKGROUND, AUTOSTART, DRAW_OVERLAY
}