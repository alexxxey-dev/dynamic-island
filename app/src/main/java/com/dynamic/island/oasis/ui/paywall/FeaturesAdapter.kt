package com.dynamic.island.oasis.ui.paywall

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager.widget.PagerAdapter
import com.dynamic.island.oasis.R

class FeaturesAdapter(private val items:List<Int>, private val context: Context) :Adapter<FeaturesAdapter.FeatureHolder>() {
    inner class FeatureHolder(private val view: TextView) : ViewHolder(view){
        fun bind(text:Int){
            view.text = context.getString(text)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureHolder {
        val dp18 = context.resources.getDimension(R.dimen.dp18).toInt()
        return FeatureHolder(TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
            setTextAppearance(R.style.MyTextWorkSans20)
            setPadding(0,dp18,0,dp18)
            gravity = Gravity.CENTER
            setTextColor(context.getColor(R.color.dark_purple_5))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        })
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: FeatureHolder, position: Int) = holder.bind(items[position])
}