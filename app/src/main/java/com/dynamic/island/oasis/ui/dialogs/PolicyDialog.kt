package com.dynamic.island.oasis.ui.dialogs

import android.text.method.LinkMovementMethod
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.DialogPolicyBinding
import com.dynamic.island.oasis.ui.BaseDialog
import com.dynamic.island.oasis.util.ext.removeLinksUnderline
import com.dynamic.island.oasis.util.ext.scaleClickListener
import org.koin.android.ext.android.inject

class PolicyDialog : BaseDialog<DialogPolicyBinding>(R.layout.dialog_policy) {
    private val prefs by inject<PrefsUtil>()


    override fun DialogPolicyBinding.initialize() {

        text.movementMethod = LinkMovementMethod.getInstance()
        text.removeLinksUnderline()
        ok.scaleClickListener {

            title.text = getString(R.string.accessibility_data)
            text.text = getString(R.string.accessibility_disclosure)

            ok.scaleClickListener {
                prefs.policyAccepted(true)
                dismissAllowingStateLoss()
            }


        }
    }
}