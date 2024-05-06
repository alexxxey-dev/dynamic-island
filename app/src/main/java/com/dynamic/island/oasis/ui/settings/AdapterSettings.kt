package com.dynamic.island.oasis.ui.settings

import android.app.Activity
import android.view.View
import androidx.core.content.ContextCompat
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.data.models.MySetting
import com.dynamic.island.oasis.databinding.ItemSettingsBinding
import com.dynamic.island.oasis.ui.animated_switch.AnimatedSwitchListener
import com.dynamic.island.oasis.util.AbstractAdapter


class AdapterSettings(private val viewModel: SettingsViewModel, private val activity: Activity) :
    AbstractAdapter<MySetting, ItemSettingsBinding>(R.layout.item_settings) {




    override fun onBind(item: MySetting, binding: ItemSettingsBinding) {
        binding.vm = viewModel
        val ctx = binding.root.context
        val index = items.indexOfFirst { it.id ==item.id }
        val locked = viewModel.subscription() || !item.isPremium
        val textColor = ContextCompat.getColor(
            ctx,
            if (locked) R.color.purple else R.color.purple_grey
        )
        val text = ctx.resources.getString(item.text)
        binding.line.visibility = if (index == items.size - 1) View.GONE else View.VISIBLE
        binding.text.text = text
        binding.text.setTextColor(textColor)
        binding.lock.visibility = if (!locked) View.VISIBLE else View.GONE
        binding.animatedSwitch.setOnClickListener {

            viewModel.onSwitchClicked( binding.animatedSwitch, item)
        }

        binding.animatedSwitch.setCheckedListener(object : AnimatedSwitchListener {


            override fun onCheckedChanged(checked: Boolean, fromUser: Boolean) {
                if(fromUser) viewModel.onSettingToggle(activity,item, checked)
            }
        })
        binding.animatedSwitch.visibility = if (!item.isColor) View.VISIBLE else View.INVISIBLE
        val enabled = viewModel.isEnabled(item)
        binding.animatedSwitch.setChecked(enabled, false)
        binding.colorCard.setOnClickListener {
            viewModel.showColorPicker(item)
        }
        binding.colorCard.visibility = if (item.isColor) View.VISIBLE else View.INVISIBLE
        val mColor = viewModel.backgroundColor()
        binding.colorBackground.setBackgroundColor(mColor)
    }


}