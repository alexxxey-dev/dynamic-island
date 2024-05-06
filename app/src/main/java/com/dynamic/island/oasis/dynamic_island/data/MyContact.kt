package com.dynamic.island.oasis.dynamic_island.data

import android.graphics.Bitmap

data class MyContact(val phoneNumber:String, val name:String, val photo: Bitmap? = null, val letter:String)