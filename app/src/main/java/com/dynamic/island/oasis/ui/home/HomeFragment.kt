package com.dynamic.island.oasis.ui.home

import android.annotation.SuppressLint

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentHomeBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.ui.main.MainActivity
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.createReceiver
import com.dynamic.island.oasis.util.ext.destroyReceiver
import com.dynamic.island.oasis.util.ext.safeNavigate
import com.dynamic.island.oasis.util.ext.scaleClickListener
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class   HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home){
    private val viewModel by activityViewModel<HomeViewModel>()

    private val adSource by inject<AdSource>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showPaywall()
        viewModel.showOnboarding()
    }

    override fun FragmentHomeBinding.initialize() {
        vm = viewModel

        requireContext().analyticsEvent("open_home_fragment")
        setupReceiver()
        setupObservers()
        reset.scaleClickListener {
            viewModel.resetSettings()
        }
    }


    private fun setupReceiver(){
        requireActivity().createReceiver(
            viewModel.receiver,
            IntentFilter().apply {
                addAction(Constants.ACTION_SEND_DI_STATE)
                addAction(Constants.ACTION_SUBSCRIPTION_DEACTIVATED)
                addAction(Constants.ACTION_SUBSCRIPTION_ACTIVATED)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSubscription()
        viewModel.init()
    }
    override fun onDestroy() {
        requireActivity().destroyReceiver(viewModel.receiver)

        super.onDestroy()
    }

    private fun setupObservers(){
        viewModel.showPaywall.observe(viewLifecycleOwner){
            (requireActivity() as MainActivity).showPaywall()
        }
        viewModel.showDestinationBundle.observe(viewLifecycleOwner){
            safeNavigate(it.first,it.second)
        }
        viewModel.showInterstitial.observe(viewLifecycleOwner){
            adSource.showInterstitial(requireActivity() as MainActivity)
        }
        viewModel.showDestination.observe(viewLifecycleOwner){
            safeNavigate(it)
        }
        viewModel.sendBroadcast.observe(viewLifecycleOwner){
            requireActivity().sendBroadcast(it)
        }
    }



}