package com.simple.launcher.retirement.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.colorBackground
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.exts.withAlpha
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.theme.ThemeColorStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

open class BaseViewModel : ViewModel() {

    val themes = ThemeColorStore.colorMapFlow

    val strings = StringResStore.stringMapFlow

    val resources: StateFlow<Map<String, Any>> = combineState(flow1 = themes, flow2 = strings, initialValue = emptyMap()) { themes, strings ->

        val data = hashMapOf<String, Any>()
        data.putAll(themes)
        data.putAll(strings)
        data
    }

    val background: StateFlow<Background> = themes.map { themeMap ->
        val backgroundColor = themeMap.colorBackground
        Background.Builder()
            .backgroundColor(backgroundColor)
            .build()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Background.Builder().build())

    open val bottomSheet: StateFlow<BottomSheetState> = themes.map { themeMap ->
        val backgroundColor = themeMap.colorBackground
        val anchorColor = themeMap.textColorSecondary

        buildBottomSheetState(
            backgroundColor = backgroundColor,
            anchorColor = anchorColor,
            showAnchor = true
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BottomSheetState.empty())

    val numpadState: StateFlow<NumpadState> = themes.map { themeMap ->
        NumpadState(
            textColor = themeMap.textColorPrimary,
            rippleColor = themeMap.colorPrimary.withAlpha(0.12f),
            deleteIconColor = themeMap.textColorPrimary,
            textSize = 24f
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NumpadState(0, 0, 0, 0f))
}

data class NumpadState(
    val textColor: Int,
    val rippleColor: Int,
    val deleteIconColor: Int,
    val textSize: Float
)
