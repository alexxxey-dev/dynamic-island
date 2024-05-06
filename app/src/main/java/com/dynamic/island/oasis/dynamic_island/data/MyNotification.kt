package com.dynamic.island.oasis.dynamic_island.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MyNotification(
    val title: String,
    val text: String,
    val packageName: String,
    val id: Int,
    val count: Int,
    val sentTimestamp: Long,
    val postTimestamp:Long
) {
    fun equalTo(other: MyNotification?):Boolean{
        if(other==null) return false
        return title == other.title && text == other.text && packageName == other.packageName && id == other.id
    }

    fun time(): String = SimpleDateFormat("HH:mm", Locale.US).format(Date(sentTimestamp))
}