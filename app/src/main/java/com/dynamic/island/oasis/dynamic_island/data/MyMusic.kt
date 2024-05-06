package com.dynamic.island.oasis.dynamic_island.data

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class MyMusic(
    val id:Int,
    val title:String,
    val artist:String,
    val appPackage:String,
    val isInteractive: MyInteractive,
    val duration:Long,
    val launchIntent:Intent? = null,
    val appLogo:Drawable? = null,
    val albumLogo:Bitmap? = null
)