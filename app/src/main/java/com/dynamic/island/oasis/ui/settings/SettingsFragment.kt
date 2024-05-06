package com.dynamic.island.oasis.ui.settings

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentSettingsBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.ui.main.MainActivity
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.createReceiver
import com.dynamic.island.oasis.util.ext.destroyReceiver
import com.dynamic.island.oasis.util.ext.safeNavigate
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel


//TODO checkbox unchecked
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(R.layout.fragment_settings) {
    private val viewModel by activityViewModel<SettingsViewModel>()
    private val adSource by inject<AdSource>()
    private val backgroundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.onReceive(context, intent)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().createReceiver(
            backgroundReceiver, IntentFilter().apply {
                addAction(Constants.ACTION_UPDATE_BG)
                addAction(Constants.ACTION_SUBSCRIPTION_DEACTIVATED)
                addAction(Constants.ACTION_SUBSCRIPTION_ACTIVATED)
            }
        )
    }


    override fun onDestroy() {
        requireActivity().destroyReceiver(backgroundReceiver)

        super.onDestroy()
    }

    override fun FragmentSettingsBinding.initialize() {
        vm = viewModel

        requireContext().analyticsEvent("show_settings_fragment")
        setupRv()
        setupObservers()

    }

    private fun setupObservers() {
        viewModel.showDestination.observe(viewLifecycleOwner) {
            safeNavigate(it)
        }
        viewModel.sendBroadcast.observe(viewLifecycleOwner) {
            requireActivity().sendBroadcast(it)
        }

        viewModel.showPaywall.observe(viewLifecycleOwner){
            (requireActivity() as MainActivity).showPaywall()
        }
        viewModel.showToast.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext().applicationContext, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSubscription()
    }

    private fun setupRv() {
        val adapter = AdapterSettings(viewModel,requireActivity())
        binding?.rvSettings?.adapter = adapter
        viewModel.showDestinationBundle.observe(viewLifecycleOwner){
            safeNavigate(it.first, it.second)
        }
        viewModel.updateList.observe(viewLifecycleOwner){
            adapter.notifyDataSetChanged()
        }
        viewModel.settingsList.observe(viewLifecycleOwner) {
            adapter.updateList(it)
        }

        viewModel.updateItem.observe(viewLifecycleOwner) {
            adapter.updateItem(it)
        }
    }

}