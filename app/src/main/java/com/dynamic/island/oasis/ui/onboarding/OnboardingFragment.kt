package com.dynamic.island.oasis.ui.onboarding

import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentOnboardingBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safeNavigate
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class OnboardingFragment: BaseFragment<FragmentOnboardingBinding>(R.layout.fragment_onboarding) {
    private val viewModel by activityViewModel<OnboardingViewModel>()

    override fun FragmentOnboardingBinding.initialize() {
        vm = viewModel

        requireContext().analyticsEvent("open_onboarding_fragment")
        setupObservers()
        setupOnBackPressed()
        setupPager()
    }

    override fun onResume() {
        super.onResume()
        viewModel.showPolicyDialog()
    }
    private fun setupObservers(){

        viewModel.setPosition.observe(viewLifecycleOwner){
            binding?.pager?.setCurrentItem(it.first,it.second)
        }
        viewModel.showDestination.observe(viewLifecycleOwner){
            safeNavigate(it)
        }
    }

    private fun setupOnBackPressed() {
        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed(this)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this@OnboardingFragment,
            onBackPressedCallback
        )
    }

    private fun setupPager(){
        val adapter = OnboardingAdapter(viewModel,this)

        val colorSelected = ContextCompat.getColor(requireContext(), R.color.pink)
        val colorUnselected = ContextCompat.getColor(requireContext(), R.color.light_pink)
        binding?.indicatorView?.apply {
            setSliderColor(colorUnselected,  colorSelected)
            setSliderWidth(resources.getDimension(R.dimen.pager_indicator_size))
            setSliderHeight(resources.getDimension(R.dimen.pager_indicator_size))
            setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.CIRCLE)
            setPageSize(adapter.itemCount)
            notifyDataChanged()

        }
        binding?.pager?.offscreenPageLimit = 1
        binding?.pager?.adapter =adapter
        binding?.pager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding?.indicatorView?.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.position.value = position
                viewModel.setSkipEnabled(position)
                binding?.indicatorView?.onPageSelected(position)
            }
        })
    }



    private fun onBackPressed(callback: OnBackPressedCallback) {
        if (binding?.pager?.currentItem != 0) {
            binding?.pager?.currentItem = (binding?.pager?.currentItem ?: 0) - 1
        } else {
            callback.isEnabled = false
            requireActivity().onBackPressed()
        }
    }
}