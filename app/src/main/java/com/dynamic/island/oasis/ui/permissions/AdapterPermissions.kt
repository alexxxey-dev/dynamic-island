package com.dynamic.island.oasis.ui.permissions

import android.app.Activity
import com.dynamic.island.oasis.data.models.MyPermission
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.ItemPermissionBinding
import com.dynamic.island.oasis.util.AbstractAdapter



class AdapterPermissions(private val viewModel: PermissionsViewModel, private val activity:Activity) :
    AbstractAdapter<MyPermission, ItemPermissionBinding>(R.layout.item_permission) {
    override fun onBind(item: MyPermission, binding: ItemPermissionBinding) {
        val ctx = binding.root.context
        binding.text.text = ctx.resources.getString(item.text)
        binding.animatedSwitch.setChecked(viewModel.isGranted(item),false)
        binding.animatedSwitch.setOnClickListener {
            viewModel.grantPermission(item,activity)
        }
    }



}