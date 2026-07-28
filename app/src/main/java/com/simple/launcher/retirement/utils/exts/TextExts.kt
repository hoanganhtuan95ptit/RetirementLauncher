package com.simple.launcher.retirement.utils.exts

import android.widget.TextView
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.BigTextBuilder
import com.simple.ui.precompute.text.emptyText
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.toBuilder
import com.simple.ui.precompute.text.with

fun TextView.setText(text: BigText?) {
    this.setText(text)
}

fun emptyText(): BigText = emptyText()

fun String.withStyleDisplayLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(57.sp()))
}

fun String.withStyleDisplayMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(45.sp()))
}

fun String.withStyleDisplaySmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(36.sp()))
}

fun String.withStyleHeadlineLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(32.sp()))
}

fun String.withStyleHeadlineMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(28.sp()))
}

fun String.withStyleHeadlineSmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(24.sp()))
}

fun String.withStyleTitleLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(22.sp()))
}

fun String.withStyleTitleMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(16.sp()))
}

fun String.withStyleTitleSmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(14.sp()))
}

fun String.withStyleBodyLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(16.sp()))
}

fun String.withStyleBodyMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(14.sp()))
}

fun String.withStyleBodySmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(12.sp()))
}

fun String.withStyleLabelLarge(): BigTextBuilder {
    return toBuilder().with(BigTextSize(14.sp()))
}

fun String.withStyleLabelMedium(): BigTextBuilder {
    return toBuilder().with(BigTextSize(12.sp()))
}

fun String.withStyleLabelSmall(): BigTextBuilder {
    return toBuilder().with(BigTextSize(11.sp()))
}
