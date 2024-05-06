package com.dynamic.island.oasis.data.models

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "my_app")
data class MyApp(
    @PrimaryKey
    val packageName: String,
    val name: String,
    var isSelected: Boolean,

){
    @Ignore
    var logo:Drawable? = null
}

