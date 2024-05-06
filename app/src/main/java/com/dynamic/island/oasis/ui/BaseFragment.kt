package com.dynamic.island.oasis.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.ui.main.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

open class BaseFragment<T : ViewDataBinding>(@LayoutRes private val layoutResId : Int) : Fragment(){
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val adSource by inject<AdSource>()
    private var _binding : T? = null
    val binding : T? get() = _binding

    open fun T.initialize(){}
    open fun sendFragmentCreated() = true
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mBinding= DataBindingUtil.inflate<T>(inflater, layoutResId, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        _binding = mBinding
        if(sendFragmentCreated())  {
            mainViewModel.onFragmentCreated(this)
        }
        mBinding.initialize()
        return mBinding.root
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}