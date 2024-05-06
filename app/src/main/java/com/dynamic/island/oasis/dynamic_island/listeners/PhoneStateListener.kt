package com.dynamic.island.oasis.dynamic_island.listeners

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.dynamic.island.oasis.dynamic_island.ui.features.call.CallViewModel

class PhoneStateListener(private val vm: CallViewModel, private val telephoneManager: TelephonyManager) {


    private val listener = object:PhoneStateListener(){
        override fun onCallStateChanged(state: Int, phoneNumber: String) {
            super.onCallStateChanged(state, phoneNumber)
            when(state){
                TelephonyManager.CALL_STATE_RINGING->vm.onCallRinging()
                TelephonyManager.CALL_STATE_OFFHOOK->vm.onCallActive()
                TelephonyManager.CALL_STATE_IDLE->vm.onCallFinished()
            }
        }
    }



    init {
        telephoneManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    fun onDestroy(){
        telephoneManager.listen(listener, PhoneStateListener.LISTEN_NONE)
    }


}