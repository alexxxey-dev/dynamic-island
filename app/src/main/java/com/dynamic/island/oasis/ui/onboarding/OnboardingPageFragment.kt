package com.dynamic.island.oasis.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.dynamic.island.oasis.Constants

class OnboardingPageFragment() : Fragment() {

    companion object{
        fun newInstance(layoutId:Int) = OnboardingPageFragment().apply {
            arguments = bundleOf(Constants.PARAM_LAYOUT_ID to layoutId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = arguments?.getInt(Constants.PARAM_LAYOUT_ID) ?: return null
        return inflater.inflate(layoutId,container,false    )
    }
}