package com.simple.launcher.retirement.presentation.base

import android.graphics.Color
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.emptyBackground
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.image.emptyImage
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.build
import com.simple.launcher.retirement.utils.text.emptyText
import com.simple.launcher.retirement.utils.text.with

/**
 * Trạng thái cho action button (ví dụ: nút Lưu, nút Dọn dẹp).
 * Bao gồm nội dung text (RichText) và background (Background utility).
 */
data class ActionState(
    val text: RichText = emptyText(),

    val image: RichImage = emptyImage(),
    val imageShow: Boolean = false,

    val background: Background? = emptyBackground()
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
    cornerRadius: Int = DP.DP_12,
    strokeWidth: Int = 0,
    strokeColor: Int = Color.TRANSPARENT,
    strokeDashGap: Int = 0,
    strokeDashWidth: Int = 0
): ActionState = ActionState(
    text = text.with(ForegroundColor(textColor), TextSize(textSize), Bold).build(),
    background = Background.Builder()
        .backgroundColor(backgroundColor)
        .cornerRadius(cornerRadius)
        .stroke(width = strokeWidth, color = strokeColor, dashWidth = strokeDashWidth, dashGap = strokeDashGap)
        .build()
)
