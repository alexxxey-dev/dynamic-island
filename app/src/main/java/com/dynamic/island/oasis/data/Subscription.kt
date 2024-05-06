package com.dynamic.island.oasis.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.revenuecat.purchases.Package


data class Subscription(
    val id:String,
    val title:String,
    val numberTitle:String,
    val description:String,
    val packageId:String,
    val priceText:String,
    val price:Double,
    val isTrial:Boolean,
    val shortDescription:String
)