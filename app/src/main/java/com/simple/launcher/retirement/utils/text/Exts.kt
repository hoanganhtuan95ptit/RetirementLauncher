package com.simple.launcher.retirement.utils.text

import android.widget.TextView


fun emptyText() = RichText("")

@Deprecated("dùng RichTextBuilder")
fun String.toRich(): RichText {

    return RichText(this)
}

@Deprecated("dùng RichTextBuilder")
fun String.with(vararg spannable: RichSpan): RichText {

    return RichText(this).withFirst(this, *spannable)
}

@Deprecated("dùng RichTextBuilder")
fun RichText.with(vararg spannable: RichSpan): RichText {

    return withFirst(this.text, *spannable)
}

@Deprecated("dùng RichTextBuilder")
fun String.withFirst(bold: String, vararg spannable: RichSpan): RichText {

    return RichText(this).withFirst(bold, *spannable)
}

@Deprecated("dùng RichTextBuilder")
fun String.withAll(textUpdate: String, vararg spannable: RichSpan): RichText {

    return RichText(this).withAll(textUpdate, *spannable)
}

@Deprecated("dùng RichTextBuilder")
fun RichText.withFirst(textUpdate: String, vararg spannable: RichSpan): RichText {
    if (textUpdate.isEmpty()) return this
    val start = text.indexOf(textUpdate)
    if (start == -1) return this

    spans.add(RichStyle(RichRange(start, start + textUpdate.length), spannable.toList()))
    return refresh()
}

@Deprecated("dùng RichTextBuilder")
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