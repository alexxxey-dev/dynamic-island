package com.dynamic.island.oasis.ui.paywall

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentPaywallCBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safePopBackstack
import com.dynamic.island.oasis.util.ext.scaleClickListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaywallCFragment : BaseFragment<FragmentPaywallCBinding>(R.layout.fragment_paywall_c) {
    private val viewModel by viewModel<PaywallViewModel>()
    override fun FragmentPaywallCBinding.initialize() {
        vm = viewModel
        text.scaleClickListener {
            findNavController().safePopBackstack()
        }
        requireContext().analyticsEvent(
            "open_paywall_fragment", bundleOf(
                "id" to "C"
            )
        )
    }
}