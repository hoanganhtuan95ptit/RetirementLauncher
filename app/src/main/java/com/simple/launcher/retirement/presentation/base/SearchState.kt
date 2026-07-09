package com.simple.launcher.retirement.presentation.base

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.size.DP
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.ColorFilter
import com.simple.ui.precompute.image.addTransform
import com.simple.ui.precompute.image.build
import com.simple.ui.precompute.image.toBuilder

data class SearchState(
    val hint: String,
    val textColor: Int,
    val hintColor: Int,
    val background: Background,
    val clearIcon: BigImage? = null
) {
    companion object {
        fun empty() = SearchState(
            hint = "",
            textColor = 0,
            hintColor = 0,
            background = Background.Builder().build()
        )
    }
}

/**
 * Tạo SearchState cho ô tìm kiếm.
 */
fun buildSearchState(
    hint: String,
    textColor: Int,
    hintColor: Int,
    backgroundColor: Int,
    cornerRadius: Int = DP.DP_12,
    clearIconColor: Int = textColor
): SearchState = SearchState(
    hint = hint,
    textColor = textColor,
    hintColor = hintColor,
    background = Background.Builder()
        .backgroundColor(backgroundColor)
        .cornerRadius(cornerRadius)
        .build(),
    clearIcon = buildClearIcon(clearIconColor)
)

/**
 * Tạo BigImage cho nút clear với màu icon được lấy từ theme.
 */
fun buildClearIcon(color: Int): BigImage = R.drawable.ic_clear
    .toBuilder()
    .addTransform(ColorFilter(color))
    .build()
