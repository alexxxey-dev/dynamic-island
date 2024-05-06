package com.dynamic.island.oasis.dynamic_island.listeners

import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

class OutsideTouchListener(private val onTouchOutside:()->Unit) :OnTouchListener{
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
           onTouchOutside()
            return true
        }
        return false
    }

}