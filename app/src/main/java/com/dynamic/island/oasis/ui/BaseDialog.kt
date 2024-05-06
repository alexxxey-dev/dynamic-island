package com.dynamic.island.oasis.ui

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R


open class BaseDialog<T : ViewDataBinding>(@LayoutRes private val layoutResId : Int) : DialogFragment() {
    private var _binding : T? = null
    val binding : T? get() = _binding

    open fun T.initialize(){}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            setStyle(STYLE_NO_TITLE, R.style.MyDialogBlur)
        } else{
            setStyle(STYLE_NO_TITLE, R.style.MyDialogDim)
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.let { window->

            val width =  (resources.displayMetrics.widthPixels * Constants.DIALOG_SIZE_TO_SCREEN).toInt()
            window.setGravity(Gravity.CENTER)

            window.attributes = window.attributes.apply {
                this.width = width
//                    this.y = -(mHeight/2)
            }

        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val mBinding= DataBindingUtil.inflate<T>(inflater, layoutResId, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

        }
        _binding = mBinding
        mBinding.initialize()
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.root?.post {

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}