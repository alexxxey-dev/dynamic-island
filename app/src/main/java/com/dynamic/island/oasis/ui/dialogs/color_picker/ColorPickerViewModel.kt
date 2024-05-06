package com.dynamic.island.oasis.ui.dialogs.color_picker

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.colorToHex

class ColorPickerViewModel(private val prefs: PrefsUtil): ViewModel() {
    val color = MutableLiveData<Int>()
    val colorHex = MutableLiveData<String>()

    val dismiss = SingleLiveEvent<Unit>()
    val setColor = SingleLiveEvent<Int>()
    val sendBroadcast = SingleLiveEvent<Intent>()



    fun init(){
        val color = prefs.backgroundColor()
        val defaultColor = prefs.defaultBackgroundColor()
        println(defaultColor)
        setColor.value = color
    }

    fun onColorSelected(newColor:Int){
        color.value = newColor
        colorHex.value = newColor.colorToHex()
    }

    fun onResetClicked(){
        setColor.value = prefs.defaultBackgroundColor()
    }


    fun onOkClicked(){
        val mColor = color.value ?: return
        prefs.backgroundColor(mColor)
        sendBroadcast.value = Intent(Constants.ACTION_UPDATE_BG)
        dismiss.value = Unit
    }


    fun onCancelClicked(){
        dismiss.value = Unit
    }
}