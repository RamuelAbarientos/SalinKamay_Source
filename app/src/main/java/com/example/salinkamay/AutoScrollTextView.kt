package com.example.salinkamay

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.Scroller
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView

class AutoScrollTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var scroller: Scroller = Scroller(context, LinearInterpolator())
    private var scrollSpeedSmall = 100  // Pixels per second for small text
    private var scrollSpeedLarge = 500  // Pixels per second for large text
    private var pauseDuration: Long = 1000  // Pause between scroll cycles
    private var initialDelay: Long = 1000  // Delay before starting scroll
    private var isAutoScrollEnabled = true
    private var isCurrentlyScrolling = false
    private var isLargeText = false

    init {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        setHorizontallyScrolling(true)
        isSelected = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && isAutoScrollEnabled) {
            postDelayed({ startScrollingIfNeeded() }, initialDelay)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isSelected = true
        if (isAutoScrollEnabled) {
            postDelayed({ startScrollingIfNeeded() }, initialDelay)
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        isSelected = true
        if (hasWindowFocus && isAutoScrollEnabled) {
            postDelayed({ startScrollingIfNeeded() }, initialDelay)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE && isAutoScrollEnabled) {
            isSelected = true
            postDelayed({ startScrollingIfNeeded() }, initialDelay)
        }
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (isAutoScrollEnabled) {
            restartScrolling() // Restart scrolling when text changes
        }
    }

    fun setLargeTextMode(isLarge: Boolean) {
        isLargeText = isLarge
        restartScrolling()
    }

    private fun startScrollingIfNeeded() {
        if (!isAutoScrollEnabled || width == 0 || text.isNullOrEmpty()) return

        val textWidth = paint.measureText(text.toString()).toInt()
        val availableWidth = width - paddingLeft - paddingRight

        if (textWidth > availableWidth) {
            isCurrentlyScrolling = true
            val scrollDistance = textWidth + availableWidth
            val scrollSpeed = if (isLargeText) scrollSpeedLarge else scrollSpeedSmall
            val duration = (scrollDistance / scrollSpeed.toFloat() * 1000).toInt()

            scroller.forceFinished(true) // Ensure scroller stops before restarting
            scroller.startScroll(0, 0, scrollDistance, 0, duration)

            invalidate()
        } else {
            isCurrentlyScrolling = false
        }
    }



    fun setAutoScrollEnabled(enabled: Boolean) {
        isAutoScrollEnabled = enabled
        if (enabled) {
            restartScrolling()
        } else {
            scroller.abortAnimation()
            scrollTo(0, 0)
        }
    }

    fun restartScrolling() {
        if (isAutoScrollEnabled) {
            scroller.forceFinished(true) // Ensure previous scroll is stopped
            scrollTo(0, 0)
            postDelayed({ startScrollingIfNeeded() }, initialDelay)
        }
    }


    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            postInvalidateOnAnimation() // Force redraw for smooth scrolling
        } else if (isCurrentlyScrolling && isAutoScrollEnabled) {
            postDelayed({
                scroller.startScroll(scrollX, 0, -scrollX, 0, 500) // Smooth reset
                postDelayed({ startScrollingIfNeeded() }, pauseDuration)
            }, pauseDuration)
        }
    }

}
