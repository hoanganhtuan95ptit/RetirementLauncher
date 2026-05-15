package com.simple.launcher.retirement.presentation.pin_setup

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PinVerifyViewModel : BaseViewModel() {

    val action: StateFlow<ActionState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: android.graphics.Color.LTGRAY

        val text = stringMap.getString(R.string.pin_verify_action)
            .toRich()
            .with(ForegroundColor(color), TextSize(18), Bold)
            
        val background = Background(
            backgroundColor = backgroundColor,
            cornerRadius_TL = DP.DP_12,
            cornerRadius_TR = DP.DP_12,
            cornerRadius_BL = DP.DP_12,
            cornerRadius_BR = DP.DP_12
        )
            
        ActionState(text = text, background = background)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())
}
