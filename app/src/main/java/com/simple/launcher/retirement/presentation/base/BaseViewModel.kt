package com.simple.launcher.retirement.presentation.base

import android.graphics.Typeface
import androidx.lifecycle.ViewModel
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.text.CustomFont
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.ThemeColorStore

open class BaseViewModel : ViewModel() {

    val strings = StringResStore.stringMapFlow

    val themes = ThemeColorStore.colorMapFlow

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
}
