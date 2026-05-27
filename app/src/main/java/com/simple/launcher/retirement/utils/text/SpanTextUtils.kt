package com.simple.launcher.retirement.utils.text

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.withTranslation
import kotlin.math.ceil

object SpanTextUtils {
    private val TEXT_PAINT = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f
        color = Color.BLACK
    }

    /** Build spannable text */
    inline fun build(builderAction: SpannableStringBuilder.() -> Unit): SpannableStringBuilder {
        return SpannableStringBuilder().apply(builderAction)
    }

    /** Append text with spans */
    fun SpannableStringBuilder.appendSpan(text: CharSequence, vararg spans: Any): SpannableStringBuilder {
        val start = length
        append(text)
        spans.forEach { setSpan(it, start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        return this
    }

    /** Calculate text width */
    fun measureWidth(text: CharSequence, textPaint: TextPaint = TEXT_PAINT): Float {
        return Layout.getDesiredWidth(text, textPaint)
    }

    /** Calculate text height (multi line support) */
    fun measureHeight(text: CharSequence, textPaint: TextPaint = TEXT_PAINT, width: Int): Int {
        val staticLayout = StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()
        return staticLayout.height
    }

    /** Full text size */
    fun measure(text: CharSequence, textPaint: TextPaint = TEXT_PAINT, width: Int = Int.MAX_VALUE): Size {
        return if (width == Int.MAX_VALUE) {
            val w = measureWidth(text, textPaint)
            val fm = textPaint.fontMetrics
            val h = fm.bottom - fm.top
            Size(width = ceil(w).toInt(), height = ceil(h).toInt())
        } else {
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(false)
                .build()
            Size(width = layout.width, height = layout.height)
        }
    }

    /** Draw spannable text manually on canvas */
    fun drawText(canvas: Canvas, text: CharSequence, textPaint: TextPaint = TEXT_PAINT, x: Float, y: Float, width: Int) {
        canvas.withTranslation(x, y) {
            val staticLayout = StaticLayout.Builder
                .obtain(text, 0, text.length, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(false)
                .build()
            staticLayout.draw(this)
        }
    }

    data class Size(val width: Int, val height: Int)
}