package com.dynamic.island.oasis.ui.paywall

import android.text.method.LinkMovementMethod
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dynamic.island.oasis.data.Subscription
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentPaywallABinding
import com.dynamic.island.oasis.databinding.ItemSubscriptionBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.util.AbstractAdapter
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.removeLinksUnderline
import com.dynamic.island.oasis.util.ext.safePopBackstack
import com.dynamic.island.oasis.util.ext.scaleClickListener
import com.dynamic.island.oasis.util.ext.showUrl
import org.koin.androidx.viewmodel.ext.android.viewModel

//TODO analytics events
class PaywallAFragment : BaseFragment<FragmentPaywallABinding>(R.layout.fragment_paywall_a) {
    private val viewModel by viewModel<PaywallViewModel>()
    private val adapterSubscription = object:
        AbstractAdapter<Subscription, ItemSubscriptionBinding>(R.layout.item_subscription){
        override fun onBind(item: Subscription, binding: ItemSubscriptionBinding) {
            binding.subscription = item
            binding.vm = viewModel
            binding.lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun FragmentPaywallABinding.initialize() {
        vm = viewModel

        requireContext().analyticsEvent("open_paywall_fragment", bundleOf(
            "id" to "A"
        ))

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
        policy.scaleClickListener {
            viewModel.showPolicy()
        }
        terms.scaleClickListener {
            viewModel.showTerms()
        }
        viewModel.showUrl.observe(viewLifecycleOwner){
            requireContext().showUrl(it)
        }
        rvPaywall.adapter = adapterSubscription
        rvPaywall.layoutManager = object:LinearLayoutManager(requireContext()){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        viewModel.showMessage.observe(viewLifecycleOwner){
            //Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
            Toast.makeText(requireContext().applicationContext, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.makePurchase.observe(viewLifecycleOwner){
            viewModel.makePurchase(requireActivity(),"A",it)
        }
        viewModel.popBackStack.observe(viewLifecycleOwner){
            requireContext().analyticsEvent("close_paywall_clicked")
            findNavController().safePopBackstack()
        }
        viewModel.allSubscriptions.observe(viewLifecycleOwner){
            adapterSubscription.updateList(it)
        }

    }
}