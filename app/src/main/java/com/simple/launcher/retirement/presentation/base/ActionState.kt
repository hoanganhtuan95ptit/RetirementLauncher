package com.simple.launcher.retirement.presentation.base

import android.graphics.Color
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.emptyBackground
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.emptyImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.emptyText
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.with

/**
 * Trạng thái cho action button (ví dụ: nút Lưu, nút Dọn dẹp).
 * Bao gồm nội dung text (BigText) và background (Background utility).
 */
data class ActionState(
    val text: BigText = emptyText(),

    val image: BigImage = emptyImage(),
    val imageShow: Boolean = false,

    val isEnabled: Boolean = true,

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
    cornerRadius: Int = 12.dp(),
    strokeWidth: Int = 0,
    strokeColor: Int = Color.TRANSPARENT,
    strokeDashGap: Int = 0,
    strokeDashWidth: Int = 0,
    isEnabled: Boolean = true
): ActionState = ActionState(
    text = text.with(BigForegroundColor(textColor), BigTextSize(textSize.dp()), BigBold).build(),
    isEnabled = isEnabled,
    background = Background.Builder()
        .backgroundColor(backgroundColor)
        .cornerRadius(cornerRadius)
        .stroke(width = strokeWidth, color = strokeColor, dashWidth = strokeDashWidth, dashGap = strokeDashGap)
        .build()
)
