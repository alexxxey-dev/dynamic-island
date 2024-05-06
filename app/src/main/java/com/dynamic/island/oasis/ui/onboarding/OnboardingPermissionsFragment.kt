package com.dynamic.island.oasis.ui.onboarding

import android.widget.Toast
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.FragmentOnboardingPermissionsBinding
import com.dynamic.island.oasis.ui.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class OnboardingPermissionsFragment: BaseFragment<FragmentOnboardingPermissionsBinding>(R.layout.fragment_onboarding_permissions) {
    private val viewModel by activityViewModel<OnboardingViewModel>()
    override fun FragmentOnboardingPermissionsBinding.initialize() {
        vm = viewModel
        activity = requireActivity()
        viewModel.showToast.observe(viewLifecycleOwner){
            Toast.makeText(requireContext().applicationContext, resources.getString(it),Toast.LENGTH_LONG).show()
        }
    }

    override fun sendFragmentCreated(): Boolean {
        return false
    }


    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }
}