package com.dynamic.island.oasis.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding




abstract class AbstractAdapter<Item, Binding : ViewBinding>(
    private val layoutId: Int
) : RecyclerView.Adapter<AbstractAdapter<Item, Binding>.AbstractViewHolder>() {
     val items = ArrayList<Item>()

    abstract fun onBind(item:Item, binding:Binding)

    open fun updateItem(item:Item){
        val index = items.indexOf(item)
        if(index < 0) return
        notifyItemChanged(index)
    }
    open fun updateList(newList: List<Item>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size


    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) =
        holder.bind(items[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AbstractViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layoutId,
            parent,
            false
        )
    )

    inner class AbstractViewHolder(private val binding: Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            onBind(item, binding)
        }
    }
}