package com.dynamic.island.oasis.ui.paywall

import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.databinding.FragmentPaywallBBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.ui.main.MainActivity
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safePopBackstack
import com.dynamic.island.oasis.util.ext.scaleClickListener
import com.dynamic.island.oasis.util.ext.showUrl
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject

class PaywallBFragment : BaseFragment<FragmentPaywallBBinding>(R.layout.fragment_paywall_b) {
    private val viewModel by viewModel<PaywallViewModel>()
    private val adSource by inject<AdSource>()

    override fun FragmentPaywallBBinding.initialize() {
        vm = viewModel
        requireContext().analyticsEvent(
            "open_paywall_fragment", bundleOf(
                "id" to "B"
            )
        )
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
        setupProducts(this)
        setupTerms(this)
        setupPager()
        setupObservers()
    }

    private fun setupProducts(binding: FragmentPaywallBBinding) {
        binding.product1.init(binding.baseLayout, viewLifecycleOwner, 0,viewModel)
        binding.product2.init(binding.baseLayout,viewLifecycleOwner,1,viewModel)
    }

    private fun setupObservers() {
        viewModel.allSubscriptions.observe(viewLifecycleOwner){
            if(it.isEmpty()) findNavController().safePopBackstack()
        }
        viewModel.showMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext().applicationContext, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.makePurchase.observe(viewLifecycleOwner) {
            viewModel.makePurchase(requireActivity(), "B", it)
        }
        viewModel.showInterstitial.observe(viewLifecycleOwner){
            adSource.showInterstitial(requireActivity() as MainActivity, true)
        }
        viewModel.popBackStack.observe(viewLifecycleOwner) {
            requireContext().analyticsEvent("close_paywall_clicked")
            findNavController().safePopBackstack()
        }
    }

    private fun setupTerms(binding: FragmentPaywallBBinding) {
        binding.apply {
            terms.setOnClickListener {
                viewModel.showTerms()
            }
            restore.setOnClickListener {
                viewModel.restorePurchases()
            }
            close.setOnClickListener {
                viewModel.close()
            }
        }

        viewModel.showUrl.observe(viewLifecycleOwner) {
            requireContext().showUrl(it)
        }

    }

    private fun setupPager() {
        val adapter = FeaturesAdapter(viewModel.features, requireContext())
        val colorSelected = ContextCompat.getColor(requireContext(), R.color.dark_purple_3)
        val colorUnselected = ContextCompat.getColor(requireContext(), R.color.white)
        binding?.indicatorView?.apply {
            setSliderColor(colorUnselected, colorSelected)
            setSliderWidth(resources.getDimension(R.dimen.pager_indicator_size))
            setSliderHeight(resources.getDimension(R.dimen.pager_indicator_size))
            setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.CIRCLE)
            setPageSize(adapter.itemCount)
            notifyDataChanged()

        }
        binding?.pager?.adapter = adapter
        binding?.pager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding?.indicatorView?.onPageScrolled(
                    position,
                    positionOffset,
                    positionOffsetPixels
                )
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding?.indicatorView?.onPageSelected(position)
            }
        })
    }
}