@file:Suppress("UNCHECKED_CAST")

package com.simple.launcher.retirement.utils.exts

import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.theme.ThemeColorStore

fun <T> Any.asObject() = this as? T

fun <T> Any.asObjectOrNull() = this as? T


fun Int?.orZero() = this ?: 0


fun Map<String, Any>.getString(resId: Int): String {
    return StringResStore.idAndNameMap[resId]?.let {
        StringResStore.stringMapFlow.value[it]
    } ?: return ""
}

fun Map<String, Any>.getColor(@AttrRes attrId: Int, @ColorInt defaultColor: Int = android.graphics.Color.BLACK): Int {
    return ThemeColorStore.idAndNameMap[attrId]?.let {
        ThemeColorStore.colorMapFlow.value[it]
    } ?: defaultColor
}