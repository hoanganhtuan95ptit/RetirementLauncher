package com.simple.launcher.retirement.presentation.base

import com.simple.launcher.retirement.utils.background.Background

data class BottomSheetState(
    val background: Background? = null,
    val anchorBackground: Background? = null,
    val showAnchor: Boolean = false
) {
    companion object {
        fun empty() = BottomSheetState()
    }
}
