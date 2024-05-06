package com.dynamic.island.oasis.dynamic_island.ui.features.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutExpandedNotificationBinding
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.listeners.OutsideTouchListener
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.util.ext.resize
import com.dynamic.island.oasis.util.ext.safeLaunch
import com.dynamic.island.oasis.util.ext.safeSetPosition
import com.dynamic.island.oasis.util.ext.setup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExpandedNotificationView(
    val context: Context,
    private val viewModel: NotificationViewModel,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutExpandedNotificationBinding>(
    context,
    viewModel,
    R.layout.layout_expanded_notification,
    true, windowManager, inflater
) {
    override val registry= LifecycleRegistry(this)



    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        val same = (previous is DiState.Notification && previous.expanded)
        if (same) return

        Logs.view("show_expanded_notification_view")
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
        Logs.view("hide_expanded_notification_view")
        binding?.pager?.adapter = null
        destroyView()
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).safeLaunch {
            if (current !is DiState.Notification || !current.expanded) {
                hide(current, previous, diParams)
                return@safeLaunch
            }

            show(current, previous, diParams)
        }
    }


    override fun setupView(binding: LayoutExpandedNotificationBinding) {
        super.setupView(binding)

        binding.root.setOnTouchListener(OutsideTouchListener { viewModel.onCollapseNotification() })

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.position.value = position
                binding.pager.resize(position)
            }
        })
        binding.pager.adapter = NotificationAdapter(this@ExpandedNotificationView, viewModel)
        (binding.pager.getChildAt(0) as RecyclerView).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
        viewModel.notificationList.observe(this){
            (binding.pager.adapter as NotificationAdapter?)?.updateList(it)

            val itemCount = binding.pager.adapter?.itemCount ?: 0
            val pos = binding.pager.currentItem
            if (pos < 0 || pos >= itemCount) return@observe
            binding.pager.resize(pos)
        }
        viewModel.setPosition.observe(this) {
            val itemCount = binding.pager.adapter?.itemCount ?: 0
            if (it < 0 || it >= itemCount) return@observe
            binding.pager.safeSetPosition(it)
            binding.pager.resize(it)
        }


    }


}