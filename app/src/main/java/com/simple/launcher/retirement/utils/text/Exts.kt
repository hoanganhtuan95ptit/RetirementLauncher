package com.simple.launcher.retirement.utils.text

import android.widget.TextView
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.BigTextBuilder
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.toBuilder
import com.simple.ui.precompute.text.with

fun TextView.setText(text: BigText?) {
    this.setText(text)
}

fun emptyText(): BigText = com.simple.ui.precompute.text.emptyText()

fun String.withStyleDisplayLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(57))
}

fun String.withStyleDisplayMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(45))
}

fun String.withStyleDisplaySmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(36))
}

fun String.withStyleHeadlineLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(32))
}

fun String.withStyleHeadlineMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(28))
}

fun String.withStyleHeadlineSmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(24))
}

fun String.withStyleTitleLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(22))
}

fun String.withStyleTitleMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(16))
}

fun String.withStyleTitleSmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(14))
}

fun String.withStyleBodyLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(16))
}

fun String.withStyleBodyMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(14))
}

fun String.withStyleBodySmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(12))
}

fun String.withStyleLabelLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(14))
}

fun String.withStyleLabelMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(12))
}

fun String.withStyleLabelSmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(11))
}
