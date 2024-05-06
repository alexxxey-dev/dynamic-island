package com.dynamic.island.oasis.dynamic_island.ui.features.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.ItemNotificationBinding
import com.dynamic.island.oasis.dynamic_island.data.MyNotification
import com.dynamic.island.oasis.util.ext.getAppLogo
import com.dynamic.island.oasis.util.ext.getAppTitle
import com.dynamic.island.oasis.util.ext.hideKeyboard
import com.dynamic.island.oasis.util.ext.showKeyboard
import com.dynamic.island.oasis.util.ext.showNotifActions
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.scaleClickListener

class NotificationAdapter(
    private val view: ExpandedNotificationView,
    private val viewModel: NotificationViewModel
) : Adapter<NotificationAdapter.NotificationHolder>() {
    private val items = ArrayList<MyNotification>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList:List<MyNotification>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationHolder {
        val binding = DataBindingUtil.inflate<ItemNotificationBinding>(
            LayoutInflater.from(parent.context), R.layout.item_notification, parent, false
        )

        return NotificationHolder(binding)
    }


    inner class NotificationHolder(private val binding: ItemNotificationBinding) :
        ViewHolder(binding.root) {
        private var replyAction: Notification.Action? = null

        fun bind(notification: MyNotification, index: Int) {
            setupView( notification, index)
            setupReply(notification)
            viewModel.actionsVisible.observe(view){
                binding.layoutActions.visibility = if(it) View.VISIBLE else View.GONE
            }
        }

        private fun showReply(action: Notification.Action) {
            if(viewModel.actionsVisible.value == false) return

            replyAction = action
            binding.replyText.showKeyboard()
            view.setFocusable(true)
            binding.layoutDefault.visibility = View.GONE
            binding.layoutReply.visibility = View.VISIBLE
        }

        private fun hideReply() {
            if (replyAction == null) return
            binding.replyText.hideKeyboard()
            binding.replyText.setText("")
            replyAction = null
            view.setFocusable(false)
            binding.layoutReply.visibility = View.GONE
            binding.layoutDefault.visibility = View.VISIBLE
        }

        private fun setupReply(notification: MyNotification) {
            binding.backgroundSecond.setOnClickListener {
                hideReply()
            }
            binding.send.scaleClickListener {
                val text = binding.replyText.text.toString()
                if(text.isBlank()) return@scaleClickListener
                viewModel.replyNotification(binding.send,replyAction, text)
                hideReply()
            }

            view.context.showNotifActions(
                binding.layoutActions,
                viewModel.actions[notification.id]
            ) {
                if (viewModel.executeNotifAction(it)) {
                    showReply(it)
                    view.context.analyticsEvent("on_show_notification_reply_clicked")
                }else{
                    view. context.analyticsEvent("on_notification_action_clicked")
                }
            }
        }

        private fun setupView(notification: MyNotification, index: Int) {
            val appLogo = view.context.packageManager.getAppLogo(notification.packageName)
            val appTitle = view.context.getAppTitle(notification.packageName)

            binding.replyText.isFocusable = true
            binding.replyText.isFocusableInTouchMode = true
            binding.close.scaleClickListener { viewModel.onUserNotificationRemove(notification) }
            if (appLogo != null) binding.appLogo.setImageDrawable(appLogo)
            if (appTitle != null) binding.appTitle.text = appTitle
            binding.time.text = notification.time()
            binding.title.text = notification.title
            binding.text.text = notification.text
            binding.appLayout.setOnClickListener { viewModel.openApp(notification) }
            binding.index.text = "${index + 1}/${items.size}"
        }


    }


    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() :Int{
        return items.size
    }


}