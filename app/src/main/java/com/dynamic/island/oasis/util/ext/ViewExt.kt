package com.dynamic.island.oasis.util.ext

import android.app.Notification
import android.content.Context
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.Logs


fun ViewPager2.safeSetPosition(pos:Int){
    try {
        setCurrentItem(pos,false    )
    }catch (ex:Exception){
        ex.printStackTrace()
    }
}


fun ViewPager2.setup(adapter: RecyclerView.Adapter<*>, onPageChanged:(Int)->Unit){
    val pager = this
    val recycler = (pager.getChildAt(0) as RecyclerView)
    pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            onPageChanged(position)
            resize(position)
        }
    })
    pager.adapter = adapter
    recycler.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
}


fun ViewPager2.resize(position:Int? = null){
    try {
        post{
            val recycler = (getChildAt(0) as RecyclerView)
            val view = recycler.findViewHolderForAdapterPosition(position ?: currentItem)?.itemView
            if(view==null){
                resize(position)
                return@post
            }
            view?.post {
                val wMeasureSpec =
                    View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
                val hMeasureSpec =
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                view.measure(wMeasureSpec, hMeasureSpec)
                val layoutHeight = layoutParams.height
                val measuredHeight = view.measuredHeight
                if (layoutHeight != measuredHeight) {
                    layoutParams = (layoutParams as ViewGroup.LayoutParams).also { lp -> lp.height = view.measuredHeight }
                }
            }
        }

    }catch (ex:Exception){
        Logs.log("viewpager resize exception")
        Logs.exception(ex)
    }

}

fun TextView.removeLinksUnderline() {
    val spannable = SpannableString(text)
    for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        spannable.setSpan(object : URLSpan(u.url) {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
    }
    text = spannable
}

 fun Context.showNotifActions(
    layoutActions: LinearLayout,
    actions: List<Notification.Action>?,
    onActionClicked: (Notification.Action) -> Unit
) {
    try {
        layoutActions.removeAllViews()
        if (actions == null) return
        actions.forEach { action ->
            val title = action.title.toString()
            if (title.isNotBlank()) {
                val view = notificationAction(title).apply {
                    scaleClickListener {
                        onActionClicked(action)
                    }
                }
                layoutActions.addView(view)
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

}

 fun Context.notificationAction(text: String): TextView {
    return TextView(this).apply {
        this.text = text
        this.isSingleLine = true
        this.isClickable =true
        this.isFocusable = true
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
        this.setTextColor(context.resources.getColor(R.color.pink))
        this.setTypeface(ResourcesCompat.getFont(context, R.font.roboto_medium))
        this.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                0,
                0,
                context.resources.getDimension(R.dimen.notif_action_margin_right).toInt(),
                0
            )
        }
    }
}
fun SeekBar?.setSeekListener(onChanged: (Int) -> Unit) {
    if (this == null) return

    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) onChanged(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}

fun View?.hideAllViews() {
    if (this == null || !(this is ViewGroup)) return
    for (i in 0 until childCount) {
        getChildAt(i).visibility = View.INVISIBLE
    }
}


fun View?.showAllViews() {
    if (this == null || !(this is ViewGroup)) return
    for (i in 0 until childCount) {
        getChildAt(i).visibility = View.VISIBLE
    }
}

fun View.setSwipeListener(
    onTouchOutside: () -> Unit,
    onLeftToRight: () -> Unit,
    onRightToLeft: () -> Unit
) {
    val min_distance = 100
    var downX = 0f
    var downY = 0f
    setOnTouchListener(object : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.getAction()) {
                MotionEvent.ACTION_OUTSIDE -> {
                    onTouchOutside()
                    return true
                }
                MotionEvent.ACTION_DOWN -> {
                    downX = event.getX()
                    downY = event.getY()
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = downX - event.getX()
                    val deltaY = downY - event.getY()

                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (Math.abs(deltaX) > min_distance) {
                            if (deltaX < 0) {
                                Logs.log("onLeftToRight")
                                onLeftToRight()
                                return true
                            }
                            if (deltaX > 0) {
                                Logs.log("onRightToLeft")
                                onRightToLeft()
                                return true
                            }
                        }
                    }
                }
            }
            return false
        }

    })
}