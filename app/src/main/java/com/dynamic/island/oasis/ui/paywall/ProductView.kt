package com.dynamic.island.oasis.ui.paywall

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.ItemProductViewBinding
import com.dynamic.island.oasis.databinding.ItemProfitViewBinding

class ProductView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val inflater = LayoutInflater.from(context)
    private val dp11 = context.resources.getDimension(R.dimen.dpN11).toInt()
    private val dp22 = context.resources.getDimension(R.dimen.dp22).toInt()
    private val parentView = DataBindingUtil.inflate<ItemProductViewBinding>(
        inflater,
        R.layout.item_product_view,
        this,
        false
    )
    private val blur = parentView.blur
    private val profitView = DataBindingUtil.inflate<ItemProfitViewBinding>(
        inflater,
        R.layout.item_profit_view,
        this,
        false
    )


    fun init(root: ViewGroup, owner: LifecycleOwner, pos: Int, vm: PaywallViewModel) {

        blur.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                blur.background.getOutline(outline)
                outline.alpha = 1f
            }
        }
        blur.clipToOutline = true
        blur.setupWith(root).setBlurRadius(1.5f)

        parentView.pos = pos
        parentView.lifecycleOwner = owner
        parentView.vm = vm

        profitView.pos = pos
        profitView.lifecycleOwner = owner
        profitView.vm = vm

        addView(parentView.root)
        parentView.rootLayout.addView(profitView.root,LayoutParams(LayoutParams.WRAP_CONTENT, dp22).apply {
            this.bottomToTop = parentView.blur.id

            this.leftToLeft = parentView.rootLayout.id
            this.rightToRight = parentView.rootLayout.id
            this.setMargins(0, 0, 0, dp11)
        })
    }
}