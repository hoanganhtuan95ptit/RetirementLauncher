package com.simple.launcher.retirement.utils.text

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.text.style.MetricAffectingSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import com.google.auto.service.AutoService
import java.util.ServiceLoader


fun emptyText() = RichText("")


fun String.toRich(): RichText {

    Log.d("tuanha", "toRich: $this")
    return RichText(this)
}

fun String.with(vararg spannable: RichSpan): RichText {

    return RichText(this).withFirst(this, *spannable)
}

fun RichText.with(vararg spannable: RichSpan): RichText {

    return withFirst(this.text, *spannable)
}

fun String.withFirst(bold: String, vararg spannable: RichSpan): RichText {

    return RichText(this).withFirst(bold, *spannable)
}

fun String.withAll(textUpdate: String, vararg spannable: RichSpan): RichText {

    return RichText(this).withAll(textUpdate, *spannable)
}

fun RichText.withFirst(textUpdate: String, vararg spannable: RichSpan): RichText {
    if (textUpdate.isEmpty()) return this
    val start = text.indexOf(textUpdate)
    if (start == -1) return this

    spans.add(RichStyle(RichRange(start, start + textUpdate.length), spannable.toList()))
    return refresh()
}

fun RichText.withAll(textUpdate: String, vararg spannable: RichSpan): RichText {
    if (textUpdate.isEmpty()) return this
    var index = text.indexOf(textUpdate)
    if (index == -1) return this

    val styleList = spannable.toList()
    val length = textUpdate.length
    while (index != -1) {
        spans.add(RichStyle(RichRange(index, index + length), styleList))
        index = text.indexOf(textUpdate, index + length)
    }

    return refresh()
}


fun TextView.setText(text: RichText?) {

    setText(text?.textChar)
}


private val richSpanConvert by lazy {
    ServiceLoader.load(RichSpanConvert::class.java).toList()
}


data class RichText(
    val text: String,
    val spans: ArrayList<RichStyle> = arrayListOf()
) {

    var textChar: CharSequence = text

    init {
        if (spans.isNotEmpty()) refresh()
    }

    fun refresh(): RichText {

        val spannable = SpannableString(text)
        spans.forEach { span ->
            span.styles.forEach { styleData ->
                val style = styleData.toAndroidSpan()
                spannable.setSpan(style, span.range.start, span.range.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        textChar = spannable

        return this
    }

    private fun RichSpan.toAndroidSpan(): CharacterStyle {

        return richSpanConvert.firstNotNullOf { it.getAndroidSpan(this) }
    }
}

data class RichStyle(
    val range: RichRange,
    val styles: List<RichSpan> = arrayListOf()
)

data class RichRange(
    val start: Int,
    val end: Int
)


open class RichSpan

interface RichSpanConvert {
    fun getAndroidSpan(richSpan: RichSpan): CharacterStyle?
}


object Bold : RichSpan()

@AutoService(RichSpanConvert::class)
class BoldConvert : RichSpanConvert {

    override fun getAndroidSpan(richSpan: RichSpan): CharacterStyle? {
        return if (richSpan is Bold) StyleSpan(Typeface.BOLD) else null
    }
}


data class RelativeSize(val proportion: Float) : RichSpan()

@AutoService(RichSpanConvert::class)
class RelativeSizeConvert : RichSpanConvert {

    override fun getAndroidSpan(richSpan: RichSpan): CharacterStyle? {
        return if (richSpan is RelativeSize) RelativeSizeSpan(richSpan.proportion) else null
    }
}


data class ForegroundColor(val color: Int) : RichSpan()

@AutoService(RichSpanConvert::class)
class ForegroundColorConvert : RichSpanConvert {

    override fun getAndroidSpan(richSpan: RichSpan): CharacterStyle? {
        return if (richSpan is ForegroundColor) ForegroundColorSpan(richSpan.color) else null
    }
}

data class RoundedOutline(
    val textSize: Float,
    val paddingHorizontal: Float = 0f,
    val paddingVertical: Float = 0f,
    val marginHorizontal: Float = 0f,
    val marginVertical: Float = 0f,
    val strokeColor: Int,
    val strokeWidth: Float = 1f,
    val cornerRadius: Float = 0f,
    val dashWidth: Float = 0f,
    val dashGap: Float = 0f
) : RichSpan()

@AutoService(RichSpanConvert::class)
class RoundedOutlineSpanConvert : RichSpanConvert {

    override fun getAndroidSpan(richSpan: RichSpan): CharacterStyle? {
        return (richSpan as? RoundedOutline)?.let(::RoundedOutlineAndroidSpan)
    }
}

private class RoundedOutlineAndroidSpan(
    private val span: RoundedOutline
) : MetricAffectingSpan(), LineBackgroundSpan {

    private val rect = RectF()

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun updateMeasureState(paint: TextPaint) {
        applyVerticalSpacing(paint)
    }

    override fun updateDrawState(tp: TextPaint) = Unit

    private fun applyVerticalSpacing(paint: TextPaint) {

        val fontMetrics = Paint.FontMetrics()

        TextPaint(paint).apply {
            textSize = span.textSize
        }.getFontMetrics(fontMetrics)

        val originalHeight = fontMetrics.descent - fontMetrics.ascent

        val targetHeight = originalHeight +
                (span.paddingVertical * 2) +
                (span.marginVertical * 2)

        paint.textSize = span.textSize * (targetHeight / originalHeight)
    }

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {

        val spanned = text as? Spanned ?: return

        val range = resolveLineSpanRange(
            spanned = spanned,
            lineStart = start,
            lineEnd = end
        ) ?: return

        val measurePaint = createMeasurePaint(paint)

        val rect = buildOutlineRect(
            measurePaint = measurePaint,
            left = left,
            baseline = baseline,
            text = text,
            lineStart = start,
            spanStart = range.first,
            spanEnd = range.last + 1
        )

        configureBackgroundPaint()

        canvas.drawRoundRect(
            rect,
            span.cornerRadius,
            span.cornerRadius,
            backgroundPaint
        )
    }

    private fun resolveLineSpanRange(
        spanned: Spanned,
        lineStart: Int,
        lineEnd: Int
    ): IntRange? {

        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        val resolvedStart = spanStart.coerceAtLeast(lineStart)
        val resolvedEnd = spanEnd.coerceAtMost(lineEnd)

        return if (resolvedStart < resolvedEnd) {
            resolvedStart until resolvedEnd
        } else {
            null
        }
    }

    private fun createMeasurePaint(source: Paint) = TextPaint(source).apply {
        textSize = span.textSize
    }

    private fun buildOutlineRect(
        measurePaint: TextPaint,
        left: Int,
        baseline: Int,
        text: CharSequence,
        lineStart: Int,
        spanStart: Int,
        spanEnd: Int
    ): RectF {

        val textBeforeWidth = measurePaint.measureText(
            text,
            lineStart,
            spanStart
        )

        val spanWidth = measurePaint.measureText(
            text,
            spanStart,
            spanEnd
        )

        val fontMetrics = measurePaint.fontMetrics

        val spanLeft = left +
                textBeforeWidth +
                span.marginHorizontal

        val spanRight = spanLeft + spanWidth

        val spanTop = baseline +
                fontMetrics.ascent -
                span.paddingVertical

        val spanBottom = baseline +
                fontMetrics.descent +
                span.paddingVertical

        rect.set(
            spanLeft - span.paddingHorizontal,
            spanTop,
            spanRight + span.paddingHorizontal,
            spanBottom
        )

        return rect
    }

    private fun configureBackgroundPaint() {

        backgroundPaint.apply {

            style = Paint.Style.STROKE

            color = span.strokeColor

            strokeWidth = span.strokeWidth

            pathEffect = if (
                span.dashWidth > 0f &&
                span.dashGap > 0f
            ) {
                DashPathEffect(
                    floatArrayOf(span.dashWidth, span.dashGap),
                    0f
                )
            } else {
                null
            }
        }
    }
}