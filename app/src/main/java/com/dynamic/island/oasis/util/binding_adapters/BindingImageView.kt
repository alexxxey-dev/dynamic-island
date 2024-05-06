package com.dynamic.island.oasis.util.binding_adapters

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

@BindingAdapter("app:imageDrawable")
fun setImageDrawable(view: ImageView, drawable: Drawable? ){
    if(drawable!=null){
        Glide.with(view.context)
            .load(drawable)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view)
    }
}