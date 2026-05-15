package com.simple.launcher.retirement.presentation.base

import android.graphics.Color
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.emptyText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with

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

/**
 * Tạo ActionState cho các nút bấm hành động (Lưu, Tiếp tục, vv).
 *
 * @param text Văn bản hiển thị trên nút.
 * @param textColor Màu chữ.
 * @param backgroundColor Màu nền nút.
 * @param textSize Kích thước chữ (mặc định 18).
 * @param cornerRadius Độ bo góc nút (mặc định DP_12).
 */
fun buildActionState(
    text: String,
    textColor: Int,
    backgroundColor: Int,
    textSize: Int = 18,
    cornerRadius: Int = DP.DP_12.toInt(),
    strokeWidth: Int = 0,
    strokeColor: Int = Color.TRANSPARENT,
    strokeDashGap: Int = 0,
    strokeDashWidth: Int = 0
): ActionState = ActionState(
    text = text.toRich().with(ForegroundColor(textColor), TextSize(textSize), Bold),
    background = Background(
        backgroundColor = backgroundColor,
        cornerRadius_TL = cornerRadius,
        cornerRadius_TR = cornerRadius,
        cornerRadius_BL = cornerRadius,
        cornerRadius_BR = cornerRadius,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        strokeDashGap = strokeDashGap,
        strokeDashWidth = strokeDashWidth
    )
)
