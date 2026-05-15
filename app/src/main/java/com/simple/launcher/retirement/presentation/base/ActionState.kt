package com.simple.launcher.retirement.presentation.base

import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.emptyText

/**
 * Trạng thái cho action button (ví dụ: nút Lưu, nút Dọn dẹp).
 * Bao gồm nội dung text (RichText) và background (Background utility).
 */
data class ActionState(
    val text: RichText,
    val background: Background? = null
) {
    companion object {
        fun empty() = ActionState(text = emptyText())
    }
}
