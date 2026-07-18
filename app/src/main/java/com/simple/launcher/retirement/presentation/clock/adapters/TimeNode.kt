package com.simple.launcher.retirement.presentation.clock.adapters

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import com.simple.launcher.retirement.utils.time.LunarCalendar
import com.simple.ui.precompute.DrawSpec
import com.simple.ui.precompute.MeasureContext
import com.simple.ui.precompute.node.Constraints
import com.simple.ui.precompute.node.EdgeInsets
import com.simple.ui.precompute.node.LayoutDimension
import com.simple.ui.precompute.node.LayoutNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    val isLunar: Boolean = false,
    val showAmPm: Boolean = false,
    override val padding: EdgeInsets = EdgeInsets.ZERO,
    override val layoutWidth: LayoutDimension = LayoutDimension.WrapContent,
    override val layoutHeight: LayoutDimension = LayoutDimension.WrapContent
) : LayoutNode() {

    override fun measure(ctx: MeasureContext, c: Constraints, x: Int, y: Int): TimeSpec {

        val p = padding

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

        // Tính chiều cao nhanh bằng FontMetrics (thường clock chỉ có 1 dòng)
        val metrics = paint.fontMetrics
        val lineHeight = (metrics.descent - metrics.ascent).toInt()

        val contentH = lineHeight + p.vertical

        // Nếu width là WrapContent thì mới cần measure thực tế để lấy độ rộng
        val usedWidth = if (layoutWidth is LayoutDimension.WrapContent) {
            val timeStr = buildTimeText()
            val layout = buildLayout(timeStr, paint, innerWidth)
            (0 until layout.lineCount)
                .maxOfOrNull { layout.getLineWidth(it) }
                ?.let { ceil(it.toDouble()).toInt() }
                ?: 0
        } else {
            0
        }

        val contentW = (if (layoutWidth is LayoutDimension.WrapContent) usedWidth else 0) + p.horizontal

        val w = layoutWidth.resolveSize(contentW, c.maxWidth)
        val h = layoutHeight.resolveSize(contentH, c.maxHeight)

        return TimeSpec(x, y, w, h, p.left, p.top, this, paint, innerWidth)
    }

    fun buildTimeText(): String {
        return if (isLunar) {
            LunarCalendar.getLunarDateString(Date(), pattern)
        } else {
            val formatPattern = if (showAmPm) "$pattern a" else pattern
            SimpleDateFormat(formatPattern, Locale.getDefault()).format(Date())
        }
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
    override val node: TimeNode,
    private val paint: TextPaint,
    private val innerWidth: Int
) : DrawSpec() {

    private var layout: StaticLayout = node.buildLayout(node.buildTimeText(), paint, innerWidth)

    private var attachedView: View? = null
    private var coroutineScope: CoroutineScope? = null

    private fun updateTime() {

        val timeStr = node.buildTimeText()
        if (timeStr == layout.text.toString()) return

        layout = node.buildLayout(timeStr, paint, innerWidth)
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

        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        coroutineScope?.launch {

            while (isActive) {

                updateTime()
                delay(1000)
            }
        }
    }

    override fun onDetachedFromWindow(view: View) {

        coroutineScope?.cancel()
        coroutineScope = null
        attachedView = null
    }

    override fun withPosition(newLeft: Int, newTop: Int): DrawSpec {

        if (newLeft == left && newTop == top) return this

        return TimeSpec(
            newLeft, newTop, width, height, contentLeft, contentTop, node, paint, innerWidth
        ).apply {
            // Giữ lại layout hiện tại để tránh nháy khi reposition
            this.layout = this@TimeSpec.layout
        }
    }
}
