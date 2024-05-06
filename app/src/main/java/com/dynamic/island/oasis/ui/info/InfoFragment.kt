package com.dynamic.island.oasis.ui.info


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentInfoBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.ui.main.MainActivity
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.collapse
import com.dynamic.island.oasis.util.ext.createReceiver
import com.dynamic.island.oasis.util.ext.destroyReceiver
import com.dynamic.island.oasis.util.ext.launchEmail
import com.dynamic.island.oasis.util.ext.safeNavigate
import com.dynamic.island.oasis.util.ext.shareApp
import com.dynamic.island.oasis.util.ext.showUrl
import com.dynamic.island.oasis.util.ext.expand
import com.dynamic.island.oasis.util.ext.hideAlpha
import com.dynamic.island.oasis.util.ext.safeLaunch
import com.dynamic.island.oasis.util.ext.scaleClickListener
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class InfoFragment: BaseFragment<FragmentInfoBinding>(R.layout.fragment_info) {
    private val viewModel by viewModel<InfoViewModel>()
    private val receiver = object:BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent) {
            viewModel.onReceive(intent)
        }
    }

    override fun FragmentInfoBinding.initialize() {
        vm = viewModel
        requireContext().createReceiver(receiver, IntentFilter().apply {
            addAction(Constants.ACTION_SUBSCRIPTION_ACTIVATED)
            addAction(Constants.ACTION_SUBSCRIPTION_DEACTIVATED)
        })
        requireContext().analyticsEvent("open_info_fragment")
        viewModel.launchEmail.observe(viewLifecycleOwner){
            requireContext().launchEmail()
        }
        setupInfo(this)
        howToText.scaleClickListener {
            binding?.layoutInfo?.visibility = View.VISIBLE
            binding?.layoutInfo?.clearAnimation()
            viewModel.showLockGuide()
        }
        viewModel.showUrl.observe(viewLifecycleOwner){
            requireContext().showUrl(it)
        }
        viewModel.showDestination.observe(viewLifecycleOwner){
            safeNavigate(it)
        }
        viewModel.shareApp.observe(viewLifecycleOwner){
            requireContext().shareApp()
        }
        viewModel.showPaywall.observe(viewLifecycleOwner){
            (requireActivity() as MainActivity).showPaywall()
        }
        viewModel.sendBroadcast.observe(viewLifecycleOwner){
            requireContext().sendBroadcast(it)
        }
    }

    private fun setupInfo(binding:FragmentInfoBinding){
        binding.layoutInfo.visibility = View.GONE
        viewModel.showInfo.observe(viewLifecycleOwner){show->
            lifecycleScope.safeLaunch {
                if(show) {
                    binding.layoutInfo.visibility = View.VISIBLE
                    binding.layoutInfo.expand(
                        startY =0f,
                        startX = 1f,
                        time = 250,
                        pivotX = 0f,
                        pivotY = 0f
                    )
                } else {
                    binding.layoutInfo.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroy() {
        requireContext().destroyReceiver(receiver)
        super.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        viewModel.loadSubscription()
    }
}