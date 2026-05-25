package com.simple.launcher.retirement.utils.text

class RichTextBuilder(val text: String) {

    private val spans: ArrayList<RichStyle> = arrayListOf()

    fun with(vararg spans: RichSpan): RichTextBuilder {

        return withFirst(text, *spans)
    }

    fun withFirst(text: String, vararg spans: RichSpan): RichTextBuilder {

        if (text.isEmpty()) return this
        val start = this.text.indexOf(text)
        if (start == -1) return this

        this.spans.add(RichStyle(RichRange(start, start + text.length), spans.toList()))

        return this
    }

    fun withAll(text: String, vararg spans: RichSpan): RichTextBuilder {

        if (text.isEmpty()) return this
        var index = this.text.indexOf(text)
        if (index == -1) return this

        val styleList = spans.toList()
        val length = text.length
        while (index != -1) {
            this.spans.add(RichStyle(RichRange(index, index + length), styleList))
            index = text.indexOf(text, index + length)
        }

        return this
    }

    // ── Build ───────────────────────────────────────────────────────────

    fun build(): RichText {

        return RichText(
            text = text,
            spans = spans
        )
    }
}