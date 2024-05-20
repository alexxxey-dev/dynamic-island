package com.dynamic.island.oasis.ui.dialogs

import androidx.lifecycle.lifecycleScope
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.DialogPermissionBinding
import com.dynamic.island.oasis.ui.BaseDialog
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safeLaunch
import com.dynamic.island.oasis.util.ext.scaleClickListener
import org.koin.android.ext.android.inject
import java.lang.IllegalStateException

class PermissionDialog : BaseDialog<DialogPermissionBinding>(R.layout.dialog_permission) {
    private val permissions by inject<PermissionsUtil>()
    override fun DialogPermissionBinding.initialize() {
        val type = arguments?.getSerializable(Constants.PARAM_PERMISSION_TYPE) as PermissionType
        icon.setImageResource(when(type){
            PermissionType.DRAW_OVERLAY-> R.drawable.ic_dialog_confirm
            PermissionType.NOTIF-> R.drawable.ic_dialog_notifications
            else -> throw IllegalStateException("Unknown permission")
        })
        title.setText(when(type){
            PermissionType.DRAW_OVERLAY-> R.string.permission_overlay_title
            PermissionType.NOTIF-> R.string.permission_notif_title
            else -> throw IllegalStateException("Unknown permission")

        })
        text.setText(when(type){
            PermissionType.DRAW_OVERLAY-> R.string.permission_overlay_text
            PermissionType.NOTIF-> R.string.permission_notif_text
            else -> throw IllegalStateException("Unknown permission")
        })
        cancel.scaleClickListener {
            requireContext().analyticsEvent("cancel_permission_dialog")
            dismissAllowingStateLoss()
        }
        ok.scaleClickListener {
             lifecycleScope.safeLaunch {
                 requireContext().analyticsEvent("grant_permission_dialog")
                 permissions.grant(type,requireActivity())
                 dismissAllowingStateLoss()
             }

        }
    }




}