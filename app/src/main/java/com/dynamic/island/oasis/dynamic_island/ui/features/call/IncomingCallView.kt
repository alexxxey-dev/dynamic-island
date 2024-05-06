package com.dynamic.island.oasis.dynamic_island.ui.features.call

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutIncomingCallBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.data.MyContact
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.util.ext.analyticsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IncomingCallView(
    private val viewModel: CallViewModel,
    private val context: Context,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutIncomingCallBinding>(
    context,
    viewModel,
    R.layout.layout_incoming_call,
    true,windowManager, inflater
) {
    override val registry= LifecycleRegistry(this)



    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        Logs.view("show_incoming_call_view")
        binding = createView()
        setupView(binding!!)
        expand(
            pivotY = 0f,
            diParams = diParams
        )
    }


    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        collapse(
            pivotY = 0f,
            diParams = diParams
        )
        Logs.view("hide_incoming_call_view")
        destroyView()
    }
    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).launch {
            if (current !is DiState.IncomingCall) {
                hide(current, previous, diParams)
                return@launch
            }
            show(current, previous, diParams)
        }
    }


    override fun setupView(binding: LayoutIncomingCallBinding) {
        super.setupView(binding)
        viewModel.notifActions.observe(this) {
            showNotifActions(it) { action ->
                viewModel.executeNotifAction(action)
                context.analyticsEvent("on_action_incoming_call_clicked")
            }
        }
        viewModel.contact.observe(this) {
            showContact(it)
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