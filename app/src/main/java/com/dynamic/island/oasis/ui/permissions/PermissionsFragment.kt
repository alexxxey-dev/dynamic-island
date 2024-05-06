package com.dynamic.island.oasis.ui.permissions

import android.widget.Toast
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentPermissionsBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.util.ext.analyticsEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class PermissionsFragment: BaseFragment<FragmentPermissionsBinding>(R.layout.fragment_permissions) {
    private val viewModel by activityViewModel<PermissionsViewModel>()

    private val adSource by inject<AdSource>()
    override fun FragmentPermissionsBinding.initialize() {
        vm = viewModel

        requireContext().analyticsEvent("open_permissions_fragment")
        setupRv()
        setupObservers()
    }

    private fun setupObservers(){
        viewModel.showToast.observe(viewLifecycleOwner){
            Toast.makeText(requireContext().applicationContext, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRv(){
        val adapter = AdapterPermissions(viewModel, requireActivity())
        binding?.rvPermissions?.adapter = adapter
        viewModel.permissions.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }
        viewModel.updateItem.observe(viewLifecycleOwner){
            adapter.updateItem(it)
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSubscription()
        viewModel.updatePermissions()
    }
}