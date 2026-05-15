package com.simple.launcher.retirement.presentation.base

import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.size.toPx

data class BottomSheetState(
    val background: Background? = null,
    val anchorBackground: Background? = null,
    val showAnchor: Boolean = false
) {
    companion object {
        fun empty() = BottomSheetState()
    }
}

/**
 * Tạo BottomSheetState cho background và anchor của BottomSheet.
 *
 * @param backgroundColor Màu nền của BottomSheet.
 * @param anchorColor Màu của thanh kéo (anchor).
 * @param showAnchor Hiển thị thanh kéo hay không.
 * @param cornerRadius Độ bo góc trên (mặc định 24dp).
 */
fun buildBottomSheetState(
    backgroundColor: Int,
    anchorColor: Int,
    showAnchor: Boolean = true,
    cornerRadius: Int = DP.DP_24.toInt()
): BottomSheetState = BottomSheetState(
    background = Background(
        backgroundColor = backgroundColor,
        cornerRadius_TL = cornerRadius,
        cornerRadius_TR = cornerRadius
    ),
    anchorBackground = Background(
        backgroundColor = anchorColor,
        cornerRadius_TL = 100.toPx(),
        cornerRadius_TR = 100.toPx(),
        cornerRadius_BL = 100.toPx(),
        cornerRadius_BR = 100.toPx(),
    ),
    showAnchor = showAnchor
)
