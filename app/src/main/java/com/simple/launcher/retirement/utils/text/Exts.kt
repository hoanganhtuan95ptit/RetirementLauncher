package com.simple.launcher.retirement.utils.text

import android.widget.TextView

fun emptyText() = RichText("")

fun TextView.setText(text: RichText?) {

    setText(text?.textChar)
}

fun String.toBuilder(): RichTextBuilder {

    return RichTextBuilder(this)
}

fun String.with(vararg spannable: RichSpan): RichTextBuilder {

    return toBuilder().with(*spannable)
}

fun String.withFirst(bold: String, vararg spannable: RichSpan): RichTextBuilder {

    return toBuilder().withFirst(bold, *spannable)
}

fun String.withAll(textUpdate: String, vararg spannable: RichSpan): RichTextBuilder {

    return toBuilder().withAll(textUpdate, *spannable)
}

fun RichTextBuilder.with(vararg spans: RichSpan): RichTextBuilder {

    return withFirst(text, *spans)
}

fun RichTextBuilder.withFirst(text: String, vararg spans: RichSpan): RichTextBuilder {

    if (text.isEmpty()) return this
    val start = this.text.indexOf(text)
    if (start == -1) return this

    add(RichStyle(RichRange(start, start + text.length), spans.toList()))

    return this
}

fun RichTextBuilder.withAll(text: String, vararg spans: RichSpan): RichTextBuilder {

    if (text.isEmpty()) return this
    var index = this.text.indexOf(text)
    if (index == -1) return this

    val styleList = spans.toList()
    val length = text.length
    while (index != -1) {
        add(RichStyle(RichRange(index, index + length), styleList))
        index = this.text.indexOf(text, index + length)
    }

    return this
}

fun RichTextBuilder.build(): RichText = RichText(
    text = text,
    spans = richStyles
)