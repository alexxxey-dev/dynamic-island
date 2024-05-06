package com.dynamic.island.oasis.ui.dialogs.color_picker

import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.DialogColorPickerBinding
import com.dynamic.island.oasis.ui.BaseDialog
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.scaleClickListener
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class ColorPickerDialog : BaseDialog<DialogColorPickerBinding>(R.layout.dialog_color_picker) {
    private val viewModel by viewModel<ColorPickerViewModel>()
    override fun DialogColorPickerBinding.initialize() {
        vm = viewModel
        requireContext().analyticsEvent("open_color_picker_dialog")
        colorPicker.setColorListener(object:ColorEnvelopeListener{
            override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                 viewModel.onColorSelected(envelope.color)

            }
        })
        colorPicker.setLifecycleOwner(viewLifecycleOwner)
        colorPicker.preferenceName = requireContext().packageName
        colorPicker.attachBrightnessSlider(brightnessPicker)
        colorPicker.post { viewModel.init() }


        bCancel.scaleClickListener {
            viewModel.onCancelClicked()
        }
        bOk.scaleClickListener { viewModel.onOkClicked() }
        bReset.scaleClickListener { viewModel.onResetClicked() }
        viewModel.setColor.observe(viewLifecycleOwner){
            colorPicker.post {
                colorPicker.setHsvPaletteDrawable()
                colorPicker.selectByHsvColor(it)
            }
        }
        viewModel.sendBroadcast.observe(viewLifecycleOwner){
            requireActivity().sendBroadcast(it)
        }
        viewModel.dismiss.observe(viewLifecycleOwner){
            dismissAllowingStateLoss()
        }
    }
}