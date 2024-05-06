package com.dynamic.island.oasis.util.binding_adapters

import android.widget.SeekBar
import androidx.databinding.BindingAdapter

@BindingAdapter("app:onStopTracking")
fun setTrackingListener(view: SeekBar, onProgressChanged: (Int, Boolean) -> Unit){
    view.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            onProgressChanged(seekBar.progress,true)
        }
    })
}
@BindingAdapter("app:onProgressChanged")
fun setProgressListener(view: SeekBar, onProgressChanged:(Int, Boolean)->Unit){
    view.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChanged(progress,fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    })
}