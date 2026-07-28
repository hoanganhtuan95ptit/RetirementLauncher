package com.simple.launcher.retirement.presentation.views.numpad

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.withAlpha
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NumpadViewModel : BaseViewModel() {

    val numpadState: StateFlow<NumpadState> = themes.map { themeMap ->
        NumpadState(
            textColor = themeMap.textColorPrimary,
            rippleColor = themeMap.colorPrimary.withAlpha(0.12f),
            deleteIconColor = themeMap.textColorPrimary,
            textSize = 24f,
            keys = listOf(
                NumpadKeyItem(label = "1", value = "1"),
                NumpadKeyItem(label = "2", value = "2"),
                NumpadKeyItem(label = "3", value = "3"),
                NumpadKeyItem(label = "4", value = "4"),
                NumpadKeyItem(label = "5", value = "5"),
                NumpadKeyItem(label = "6", value = "6"),
                NumpadKeyItem(label = "7", value = "7"),
                NumpadKeyItem(label = "8", value = "8"),
                NumpadKeyItem(label = "9", value = "9"),
                NumpadKeyItem(label = "0", value = "0")
            )
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NumpadState.empty())

    data class NumpadKeyItem(
        val label: String,
        val value: String
    )

    data class NumpadState(
        val textColor: Int,
        val rippleColor: Int,
        val deleteIconColor: Int,
        val textSize: Float,
        val keys: List<NumpadKeyItem>
    ) {
        companion object {
            fun empty() = NumpadState(
                textColor = 0,
                rippleColor = 0,
                deleteIconColor = 0,
                textSize = 0f,
                keys = emptyList()
            )
        }
    }
}
