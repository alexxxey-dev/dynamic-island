package com.dynamic.island.oasis.util.binding_adapters

import androidx.databinding.BindingAdapter
import com.dynamic.island.oasis.ui.animated_switch.AnimatedSwitch
import com.dynamic.island.oasis.ui.animated_switch.AnimatedSwitchListener

@BindingAdapter("app:checked")
fun setChecked(view: AnimatedSwitch, checked:Boolean?){
    if(checked!=null){
        view.setChecked(checked, false)
    }
}


