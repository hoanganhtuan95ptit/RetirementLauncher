package com.simple.launcher.retirement.presentation.base

import android.graphics.Typeface
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.size.toPx
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.ColorFilter
import com.simple.ui.precompute.image.addTransform
import com.simple.ui.precompute.image.build
import com.simple.ui.precompute.image.toBuilder
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.emptyText
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigCustomFont
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.with

/**
 * Trạng thái toolbar được expose từ ViewModel.
 * Fragment chỉ observe và bind — không tự tính toán styling.
 *
 * @param title    Tiêu đề đã được xử lý sẵn (màu, size, font dưới dạng spans).
 * @param backIcon Icon nút back với màu từ theme (null = ẩn nút back).
 */
data class ToolbarState(
    val title: BigText = emptyText(),
    val backIcon: BigImage? = null
) {
    companion object {
        fun empty() = ToolbarState(title = emptyText())
    }
}

/**
 * Tạo BigText cho toolbar title với màu, size và font được truyền từ ViewModel.
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
): BigText = text
    .withStyleBodyLarge()
    .with(BigBold, BigForegroundColor(color))
    .build()

/**
 * Tạo BigImage cho nút back với màu icon được lấy từ theme.
 * Fragment chỉ cần setImage(backIcon) mà không tự hardcode drawable hay color.
 *
 * @param color Màu tint (lấy từ themeMap, ví dụ: textColorPrimary).
 */
fun buildBackIcon(color: Int): BigImage = R.drawable.ic_back
    .toBuilder()
    .addTransform(ColorFilter(color))
    .build()
