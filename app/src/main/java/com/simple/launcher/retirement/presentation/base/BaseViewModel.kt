package com.simple.launcher.retirement.presentation.base

import android.graphics.Color
import android.graphics.Typeface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.size.toPx
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.CustomFont
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.ThemeColorStore
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

open class BaseViewModel : ViewModel() {

    val strings = StringResStore.stringMapFlow

    val themes = ThemeColorStore.colorMapFlow

    val background: StateFlow<Background> = themes.map { themeMap ->
        val backgroundColor = themeMap.getColor(android.R.attr.colorBackground) ?: Color.WHITE
        Background(backgroundColor = backgroundColor)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Background())

    open val bottomSheet: StateFlow<BottomSheetState> = themes.map { themeMap ->
        val backgroundColor = themeMap.getColor(android.R.attr.colorBackground) ?: Color.WHITE
        val anchorColor = themeMap.getColor(android.R.attr.textColorSecondary) ?: Color.LTGRAY

        buildBottomSheetState(
            backgroundColor = backgroundColor,
            anchorColor = anchorColor,
            showAnchor = true
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BottomSheetState.empty())

    /**
     * Tạo RichText cho toolbar title với màu, size và font được truyền từ ViewModel.
     * Adapter/Fragment chỉ cần setText(title) mà không tự xử lý style.
     *
     * @param text     Chuỗi văn bản tiêu đề (đã được resolve từ stringMap).
     * @param color    Màu chữ (lấy từ themeMap, ví dụ: textColorPrimary).
     * @param sizeDip  Kích thước font theo dp (mặc định 18dp).
     * @param typeface Font áp dụng (mặc định sans-serif-medium).
     */
    protected fun buildToolbarTitle(
        text: String,
        color: Int,
        sizeDip: Int = 18,
        typeface: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    ): RichText = text.toRich().with(
        ForegroundColor(color),
        TextSize(sizeDip),
        CustomFont(typeface)
    )

    /**
     * Tạo RichImage cho nút back với màu icon được lấy từ theme.
     * Fragment chỉ cần setImage(backIcon) mà không tự hardcode drawable hay color.
     *
     * @param color Màu tint (lấy từ themeMap, ví dụ: textColorPrimary).
     */
    protected fun buildBackIcon(color: Int): RichImage = ImageRes(R.drawable.ic_back, color)

    /**
     * Tạo ActionState cho các nút bấm hành động (Lưu, Tiếp tục, vv).
     *
     * @param text Văn bản hiển thị trên nút.
     * @param textColor Màu chữ.
     * @param backgroundColor Màu nền nút.
     * @param textSize Kích thước chữ (mặc định 18).
     * @param cornerRadius Độ bo góc nút (mặc định DP_12).
     */
    protected fun buildActionState(
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

    /**
     * Tạo BottomSheetState cho background và anchor của BottomSheet.
     *
     * @param backgroundColor Màu nền của BottomSheet.
     * @param anchorColor Màu của thanh kéo (anchor).
     * @param showAnchor Hiển thị thanh kéo hay không.
     * @param cornerRadius Độ bo góc trên (mặc định 24dp).
     */
    protected fun buildBottomSheetState(
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
}
