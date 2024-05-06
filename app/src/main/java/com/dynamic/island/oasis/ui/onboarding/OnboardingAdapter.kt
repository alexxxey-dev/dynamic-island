package com.dynamic.island.oasis.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(private val viewModel:OnboardingViewModel, private val fragment: Fragment):FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int) = viewModel.createFragment(position)
    override fun getItemCount() = viewModel.fragmentsCount()
}