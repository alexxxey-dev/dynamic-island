package com.dynamic.island.oasis.ui.dialogs.rate

import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.launchEmail
import com.dynamic.island.oasis.util.ext.launchMarket

class RateViewModel : ViewModel() {
    val dismiss = SingleLiveEvent<Unit>()
    val toast = SingleLiveEvent<Int>()

    fun onRateClicked(view: View, rating:Float) {
        val context = view.context

        context.analyticsEvent(
            "on_rate_clicked",
            bundleOf("rating" to rating)
        )
        if(rating>=4){
            context.launchMarket()
        } else{
            toast.value = R.string.bad_rating
            context.launchEmail()
        }

        dismiss.value = Unit
    }


    fun onCancelClicked(view: View) {
        val context = view.context
        context.analyticsEvent("cancel_rate_dialog")
        dismiss.value = Unit
    }
}