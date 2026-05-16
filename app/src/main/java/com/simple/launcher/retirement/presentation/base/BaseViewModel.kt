package com.simple.launcher.retirement.presentation.base

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.string.StringResStore
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
        val backgroundColor = themeMap.getColor(android.R.attr.colorBackground, Color.WHITE)
        Background(backgroundColor = backgroundColor)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Background())

    open val bottomSheet: StateFlow<BottomSheetState> = themes.map { themeMap ->
        val backgroundColor = themeMap.getColor(android.R.attr.colorBackground, Color.WHITE)
        val anchorColor = themeMap.getColor(android.R.attr.textColorSecondary, Color.LTGRAY)

        buildBottomSheetState(
            backgroundColor = backgroundColor,
            anchorColor = anchorColor,
            showAnchor = true
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BottomSheetState.empty())
}
