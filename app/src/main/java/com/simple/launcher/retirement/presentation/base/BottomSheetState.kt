package com.simple.launcher.retirement.presentation.base

import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.exts.dp

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
    cornerRadius: Int = 24.dp()
): BottomSheetState = BottomSheetState(
    background = Background.Builder()
        .backgroundColor(backgroundColor)
        .cornerRadiusTop(cornerRadius)
        .build(),
    anchorBackground = Background.Builder()
        .backgroundColor(anchorColor)
        .cornerRadius(100.dp())
        .build(),
    showAnchor = showAnchor
)
