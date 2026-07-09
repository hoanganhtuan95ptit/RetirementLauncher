package com.simple.launcher.retirement.presentation.home.services.clock

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import com.simple.ui.precompute.DrawSpec
import com.simple.ui.precompute.MeasureContext
import com.simple.ui.precompute.node.Constraints
import com.simple.ui.precompute.node.EdgeInsets
import com.simple.ui.precompute.node.LayoutDimension
import com.simple.ui.precompute.node.LayoutNode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

/**
 * A terminal node that displays formatted time and updates itself every minute.
 * Similar to [com.simple.ui.precompute.node.TextNode] but specialized for time.
 */
data class TimeNode(
    val pattern: String,
    val textSizePx: Float,
    val color: Int,
    val typeface: Typeface? = null,
    val isBold: Boolean = false,
    override val padding: EdgeInsets = EdgeInsets.ZERO,
    override val layoutWidth: LayoutDimension = LayoutDimension.WrapContent,
    override val layoutHeight: LayoutDimension = LayoutDimension.WrapContent
) : LayoutNode() {

    override fun measure(ctx: MeasureContext, c: Constraints, x: Int, y: Int): TimeSpec {
        val p = padding
        val timeStr = SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
        
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = textSizePx
            color = this@TimeNode.color
            typeface = if (isBold) {
                Typeface.create(this@TimeNode.typeface, Typeface.BOLD)
            } else {
                this@TimeNode.typeface
            }
        }

        val maxWidth = layoutWidth.maxForMeasure(c.maxWidth)
        val innerWidth = (maxWidth - p.horizontal).coerceAtLeast(0)

        val layout = buildLayout(timeStr, paint, innerWidth)
        
        val usedWidth = (0 until layout.lineCount)
            .maxOfOrNull { layout.getLineWidth(it) }
            ?.let { ceil(it.toDouble()).toInt() }
            ?: 0

        val contentW = usedWidth.coerceAtMost(innerWidth) + p.horizontal
        val contentH = layout.height + p.vertical
        
        val w = layoutWidth.resolveSize(contentW, c.maxWidth)
        val h = layoutHeight.resolveSize(contentH, c.maxHeight)

        return TimeSpec(x, y, w, h, p.left, p.top, layout, this, paint, innerWidth)
    }

    private fun LayoutDimension.maxForMeasure(parentMax: Int): Int =
        when (this) {
            is LayoutDimension.Fixed -> if (parentMax == Int.MAX_VALUE) px else px.coerceAtMost(parentMax)
            LayoutDimension.MatchParent -> parentMax
            LayoutDimension.WrapContent -> parentMax
        }.coerceAtLeast(0)

    private fun LayoutDimension.resolveSize(contentSize: Int, parentMax: Int): Int {
        val resolved = when (this) {
            is LayoutDimension.Fixed -> px
            LayoutDimension.MatchParent -> if (parentMax == Int.MAX_VALUE) contentSize else parentMax
            LayoutDimension.WrapContent -> contentSize
        }
        return if (parentMax == Int.MAX_VALUE) resolved else resolved.coerceAtMost(parentMax).coerceAtLeast(0)
    }

    fun buildLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.coerceAtLeast(0))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()
    }
}

class TimeSpec(
    override val left: Int,
    override val top: Int,
    override val width: Int,
    override val height: Int,
    private val contentLeft: Int,
    private val contentTop: Int,
    initialLayout: StaticLayout,
    override val node: TimeNode,
    private val paint: TextPaint,
    private val innerWidth: Int
) : DrawSpec() {

    private var layout: StaticLayout = initialLayout
    private var attachedView: View? = null

    private val tickerRunnable = object : Runnable {
        override fun run() {
            updateTime()
            val now = System.currentTimeMillis()
            val nextMinute = (now / 60000 + 1) * 60000
            attachedView?.postDelayed(this, nextMinute - now)
        }
    }

    private fun updateTime() {
        val timeStr = SimpleDateFormat(node.pattern, Locale.getDefault()).format(Date())
        val newLayout = node.buildLayout(timeStr, paint, innerWidth)
        
        if (newLayout.text == layout.text) return
        
        layout = newLayout
        attachedView?.postInvalidateOnAnimation()
    }

    override fun onDrawContent(canvas: Canvas) {
        if (contentLeft != 0 || contentTop != 0) {
            canvas.translate(contentLeft.toFloat(), contentTop.toFloat())
        }
        layout.draw(canvas)
    }

    override fun onAttachedToWindow(view: View) {
        attachedView = view
        updateTime()
        
        val now = System.currentTimeMillis()
        val nextMinute = (now / 60000 + 1) * 60000
        view.postDelayed(tickerRunnable, nextMinute - now)
    }

    override fun onDetachedFromWindow(view: View) {
        view.removeCallbacks(tickerRunnable)
        attachedView = null
    }

    override fun withPosition(newLeft: Int, newTop: Int): DrawSpec =
        TimeSpec(newLeft, newTop, width, height, contentLeft, contentTop, layout, node, paint, innerWidth)
}
