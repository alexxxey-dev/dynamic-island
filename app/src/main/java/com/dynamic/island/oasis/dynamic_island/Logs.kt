package com.dynamic.island.oasis.dynamic_island


import android.util.Log
import com.dynamic.island.oasis.BuildConfig
import com.dynamic.island.oasis.util.ext.getDeviceName
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Logs {
    private const val LOG_TAG = "MY_ACSB"
    private const val VIEW_TAG = "MY_DI_VIEW"
    private const val ACSB_LIFECYCLE = "LIFECYCLE_ACSB"
    private const val BILLING = "MY_BILLING_LOGS"
    private const val ADS = "ADS_LOGS"
    private val sdf = SimpleDateFormat("dd-MM HH:mm:ss", Locale.US)
    fun exception(ex:Exception){
        if(BuildConfig.DEBUG && !ex.message.isNullOrBlank()){
            ex.printStackTrace()
            Log.d(LOG_TAG, "error = ${ex.message}; ${Log.getStackTraceString(ex.cause)}")
        }
    }

    fun acsb(msg:Any?){
        if(BuildConfig.DEBUG){
            Log.d(ACSB_LIFECYCLE,msg.toString())
        }
    }
    fun view(msg:Any?){
        if (BuildConfig.DEBUG) {
            Log.d(VIEW_TAG, msg.toString()  )
        }
    }

    fun billing(msg: Any?){
        if(BuildConfig.DEBUG){
            Log.d(BILLING, msg.toString())
        }
        FirebaseDatabase.getInstance().getReference(sdf.format(Date())).setValue(msg.toString())
        FirebaseCrashlytics.getInstance().log(msg.toString())
    }

    fun ads(msg:Any?){
        if(BuildConfig.DEBUG){
            Log.d(ADS, msg.toString())
        }
        FirebaseCrashlytics.getInstance()
            .log("onAdFailedToShowFullScreenContent; message=${msg.toString()}")
    }
    fun log(msg: Any?) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, msg.toString()  )
        }
    }


}