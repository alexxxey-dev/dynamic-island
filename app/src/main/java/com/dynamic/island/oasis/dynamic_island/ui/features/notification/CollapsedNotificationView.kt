package com.dynamic.island.oasis.dynamic_island.ui.features.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutCollapsedNotificationBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.data.MyNotification
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.util.ext.getAppLogo
import com.dynamic.island.oasis.util.ext.showAlphaCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class CollapsedNotificationView(
    private val context: Context,
    private val viewModel: NotificationViewModel,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutCollapsedNotificationBinding>(
    context,
    viewModel,
    R.layout.layout_collapsed_notification,
    false,windowManager,inflater
) {
    override val registry= LifecycleRegistry(this)

    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        val same =  previous is DiState.Notification && !previous.expanded
        if(same) return


        Logs.view("show_collapsed_notification_view")
        binding = createView()
        setupView(binding!!)
        if(current != previous) binding?.backgroundSecond?.showAlphaCoroutine(Constants.COLLAPSE_TIME)
    }


    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        Logs.view("hide_collapsed_notification_view")
        destroyView()
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).launch {
            if (current !is DiState.Notification || current.expanded) {
                hide(current, previous, diParams)
                return@launch
            }

            show(current, previous, diParams)
        }
    }



    override fun setupView(binding: LayoutCollapsedNotificationBinding) {
        super.setupView(binding)
        binding.root.setOnClickListener { viewModel.openApp(viewModel.collapsedNotification.value) }
        binding.root.setOnLongClickListener { viewModel.onExpandNotification() }
        viewModel.collapsedNotification.observe(this) {
            if (it == null) return@observe
            showNotification(it)
        }
    }


    private fun showNotification(it: MyNotification){
        val appLogo = context.packageManager.getAppLogo(it.packageName)
        if (appLogo != null) binding?.logo?.setImageDrawable(appLogo)

        binding?.count?.text = it.count.toString()
    }
}