package com.simple.launcher.retirement.utils.text

import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import java.util.ServiceLoader

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

    companion object {

        fun Builder(text: String) = RichTextBuilder(text)
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