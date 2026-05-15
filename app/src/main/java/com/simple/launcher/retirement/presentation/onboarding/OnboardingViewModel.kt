package com.simple.launcher.retirement.presentation.onboarding

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.background.Background
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

class OnboardingViewModel : BaseViewModel() {

    val action: StateFlow<ActionState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        
        val text = stringMap.getString(R.string.onboarding_start)
            .toRich()
            .with(ForegroundColor(color), TextSize(18), Bold)
            
        val background = Background(
            backgroundColor = android.graphics.Color.LTGRAY,
            cornerRadius_TL = 12,
            cornerRadius_TR = 12,
            cornerRadius_BL = 12,
            cornerRadius_BR = 12
        )
            
        ActionState(text = text, background = background)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())
}
