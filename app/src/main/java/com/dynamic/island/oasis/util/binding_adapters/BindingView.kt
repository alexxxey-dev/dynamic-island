package com.dynamic.island.oasis.util.binding_adapters

import android.view.View
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter

@BindingAdapter("app:bottomPadding")
fun setBottomPadding(view: View, margin:Float?){
    if(margin!=null){
        view.updatePadding(bottom = margin.toInt())
    }
}

@BindingAdapter("app:backgroundColor")
fun setBackgroundColor(view: View, color:Int?){
    if(color!=null){
        try {
            view.setBackgroundColor(color)
        }catch (ex:Exception){
            ex.printStackTrace()
        }
    }
}