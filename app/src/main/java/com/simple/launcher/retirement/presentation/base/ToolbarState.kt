package com.simple.launcher.retirement.presentation.base

import android.graphics.Typeface
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.text.*

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

/**
 * Tạo RichText cho toolbar title với màu, size và font được truyền từ ViewModel.
 * Adapter/Fragment chỉ cần setText(title) mà không tự xử lý style.
 *
 * @param text     Chuỗi văn bản tiêu đề (đã được resolve từ stringMap).
 * @param color    Màu chữ (lấy từ themeMap, ví dụ: textColorPrimary).
 * @param sizeDip  Kích thước font theo dp (mặc định 18dp).
 * @param typeface Font áp dụng (mặc định sans-serif-medium).
 */
fun buildToolbarTitle(
    text: String,
    color: Int,
    sizeDip: Int = 18,
    typeface: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
): RichText = text
    .with(ForegroundColor(color), TextSize(sizeDip), CustomFont(typeface))
    .build()

/**
 * Tạo RichImage cho nút back với màu icon được lấy từ theme.
 * Fragment chỉ cần setImage(backIcon) mà không tự hardcode drawable hay color.
 *
 * @param color Màu tint (lấy từ themeMap, ví dụ: textColorPrimary).
 */
fun buildBackIcon(color: Int): RichImage = ImageRes(R.drawable.ic_back, color)
