package com.dynamic.island.oasis.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey



data class MySetting(
    val id: Int,
    val text: Int,
    val isPremium: Boolean,
    val isColor: Boolean=false
){

}

