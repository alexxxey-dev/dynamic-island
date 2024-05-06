package com.dynamic.island.oasis.ui.dialogs.rate

import android.widget.Toast
import androidx.core.os.bundleOf
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.DialogRateBinding
import com.dynamic.island.oasis.ui.BaseDialog
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.launchMarket
import com.dynamic.island.oasis.util.ext.scaleClickListener
import org.koin.androidx.viewmodel.ext.android.viewModel


class DialogRate : BaseDialog<DialogRateBinding>(R.layout.dialog_rate) {
    private val viewModel by viewModel<RateViewModel>()
    override fun DialogRateBinding.initialize() {
        vm = viewModel
        requireContext().analyticsEvent("open_rate_dialog")
        viewModel.dismiss.observe(viewLifecycleOwner){
            dismissAllowingStateLoss()
        }

        rate.scaleClickListener{
            viewModel.onRateClicked(it, rating.rating)
        }
        viewModel.toast.observe(viewLifecycleOwner){
            Toast.makeText(requireContext().applicationContext, it, Toast.LENGTH_LONG).show()
        }
        cancel.scaleClickListener {
            viewModel.onCancelClicked(it)
        }
    }
}