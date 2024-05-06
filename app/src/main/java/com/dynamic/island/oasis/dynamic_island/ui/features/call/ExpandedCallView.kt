package com.dynamic.island.oasis.dynamic_island.ui.features.call

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutExpandedCallBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.data.MyContact
import com.dynamic.island.oasis.dynamic_island.listeners.OutsideTouchListener
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class ExpandedCallView(
    private val viewModel: CallViewModel,
    private val context: Context,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutExpandedCallBinding>(
    context,
    viewModel,
    R.layout.layout_expanded_call,
    true, windowManager, inflater
) {

    override val registry= LifecycleRegistry(this)



    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        collapse(diParams = diParams)
        Logs.view("hide_expanded_call_view")
        destroyView()
    }


    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        val same = (previous is DiState.ActiveCall && previous.expanded)
        if (same) return

        Logs.view("show_expanded_call_view")
        binding = createView()
        setupView(binding!!)
        expand(diParams = diParams)
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).safeLaunch {
            if (current !is DiState.ActiveCall || !current.expanded) {
                hide(current, previous, diParams)
                return@safeLaunch
            }

            show(current, previous, diParams)
        }

    }


    override fun setupView(binding: LayoutExpandedCallBinding) {
        super.setupView(binding)
        binding?.apply {
            root.setOnClickListener { viewModel.openCallApp() }
            root.setOnTouchListener(OutsideTouchListener { viewModel.collapseCall() })
        }
        viewModel.notifActions.observe(this) {
            showNotifActions(it) { action ->
                viewModel.executeNotifAction(action)
                context.analyticsEvent("on_action_active_call_clicked")
            }
        }
        viewModel.contact.observe(this) {
            showContact(it)
        }
        viewModel.callTime.observe(this) {
            binding?.time?.text = it
        }
    }


    private fun showContact(contact: MyContact?) {
        if (contact == null) return
        binding?.let { binding ->

            binding.name.text = contact.name

            if (contact.photo != null) {
                binding.photo.visibility = View.VISIBLE
                binding.fakePhoto.visibility = View.GONE
                binding.photo.setImageBitmap(contact.photo)
            } else {
                binding.photo.visibility = View.GONE
                binding.fakePhoto.visibility = View.VISIBLE
                binding.fakePhotoText.text = contact.letter
            }

        }
    }


}