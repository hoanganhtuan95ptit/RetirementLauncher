package com.simple.launcher.retirement.presentation.base

import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.emptyText

/**
 * Trạng thái toolbar được expose từ ViewModel.
 * Fragment chỉ observe và bind — không tự tính toán styling.
 *
 * @param title    Tiêu đề đã được xử lý sẵn (màu, size, font dưới dạng spans).
 * @param backIcon Icon nút back với màu từ theme (null = ẩn nút back).
 */
data class ToolbarState(
    val title: RichText,
    val backIcon: RichImage? = null
) {
    companion object {
        fun empty() = ToolbarState(title = emptyText())
    }
}
