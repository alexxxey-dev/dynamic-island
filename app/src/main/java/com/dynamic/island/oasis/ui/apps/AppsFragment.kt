package com.dynamic.island.oasis.ui.apps


import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentAppsBinding
import com.dynamic.island.oasis.ui.BaseFragment
import com.dynamic.island.oasis.util.ext.analyticsEvent
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class AppsFragment : BaseFragment<FragmentAppsBinding>(R.layout.fragment_apps){
    private val viewModel by activityViewModel<AppsViewModel>()

    override fun FragmentAppsBinding.initialize() {
        vm = viewModel
        requireContext().analyticsEvent("open_app_fragment")
        val adapter = AppsAdapter(viewModel,viewLifecycleOwner)
        rvApps.adapter = adapter
        viewModel.query.observe(viewLifecycleOwner){
            viewModel.loadApps(requireContext(),it)
        }
        viewModel.setSelected.observe(viewLifecycleOwner){
            adapter.setSelected(it.first,it.second)
        }
        viewModel.apps.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }

    }

    override fun onDestroy() {
        viewModel.query.value = ""
        super.onDestroy()
    }
}