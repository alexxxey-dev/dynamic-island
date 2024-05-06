package com.dynamic.island.oasis.ui.apps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.dynamic.island.oasis.data.models.MyApp
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.ItemAppBinding
import com.dynamic.island.oasis.databinding.ItemAppsTopBinding

class AppsAdapter(private val viewModel: AppsViewModel, private val owner: LifecycleOwner) :
    RecyclerView.Adapter<ViewHolder>() {
    private val TYPE_APP = 1
    private val TYPE_TOP = 0
    private val apps = ArrayList<MyApp>()

    fun setSelected(selected: Boolean, index: Int) {
        apps[index].isSelected = selected
        notifyItemChanged(index + 1, AppPayload.Checkbox(selected))
    }

    override fun getItemCount() = apps.size + 1


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val latestPayload = payloads.lastOrNull()
        if (latestPayload is AppPayload.Checkbox && holder is AppViewHolder) {
            holder.bindCheckbox(latestPayload.isSelected)
        } else {
            onBindViewHolder(holder, position)
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is AppViewHolder) {
            val item = apps[position - 1]
            holder.onBind(item)
        } else if (holder is TopViewHolder) {
            holder.bind()
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_TOP else TYPE_APP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_APP -> AppViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_app,
                parent,
                false
            )
        )

        else -> TopViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_apps_top,
                parent,
                false
            )
        )
    }

    fun updateList(it: List<MyApp>) {
        apps.clear()
        apps.addAll(it)
        notifyDataSetChanged()
    }

    inner class TopViewHolder(private val binding: ItemAppsTopBinding) : ViewHolder(binding.root) {
        fun bind() {
            binding.vm = viewModel
            binding.lifecycleOwner = owner
        }
    }

    inner class AppViewHolder(private val binding: ItemAppBinding) : ViewHolder(binding.root) {
        fun bindCheckbox(isSelected: Boolean) {
            binding.checkbox.setImageResource(if (isSelected) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked)
        }

        fun onBind(app: MyApp) {
            if (app.logo != null) binding.logo.setImageDrawable(app.logo)
            val position = apps.indexOf(app)

            binding.view.visibility = if (position == apps.size - 1) View.GONE else View.VISIBLE
            binding.app = app
            binding.lifecycleOwner = owner
            binding.checkbox.setImageResource(if (app.isSelected) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked)

            binding.layoutCheckbox.setOnClickListener {
                val selected = !app.isSelected
                binding.checkbox.setImageResource(if (selected) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked)
                viewModel.onAppSelected(selected, app)
            }
        }
    }


    sealed class AppPayload {
        data class Checkbox(val isSelected: Boolean) : AppPayload()
    }


}