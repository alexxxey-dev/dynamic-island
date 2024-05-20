package com.dynamic.island.oasis.ui.dialogs

import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.databinding.DialogLockBinding
import com.dynamic.island.oasis.ui.BaseDialog
import com.dynamic.island.oasis.ui.home.HomeViewModel
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.scaleClickListener
import com.dynamic.island.oasis.util.ext.showUrl
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class LockDialog:BaseDialog<DialogLockBinding>(R.layout.dialog_lock) {
    private val prefs by inject<PrefsUtil>()
    private val homeVM by activityViewModel<HomeViewModel>()
    override fun DialogLockBinding.initialize() {
        cancel.scaleClickListener {
            requireContext().analyticsEvent("lock_dialog_cancel")
            prefs.showLockDialog(false)
            homeVM.startStop(it)
            dismissAllowingStateLoss()
        }
        howToText.scaleClickListener {
            requireContext().analyticsEvent("lock_dialog_guide")
            val url = homeVM.loadLockGuide()
            requireContext().showUrl(url)
        }
        ok.scaleClickListener {
            requireContext().analyticsEvent("lock_dialog_ok")
            homeVM.startStop(it)
            dismissAllowingStateLoss()
        }
    }
}