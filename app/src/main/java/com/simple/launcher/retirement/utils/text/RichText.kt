package com.simple.launcher.retirement.utils.text

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.TextView
import com.google.auto.service.AutoService
import java.util.ServiceLoader


fun emptyText() = RichText("")


fun String.toRich(): RichText {

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