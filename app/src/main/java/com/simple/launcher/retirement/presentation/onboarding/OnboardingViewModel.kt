package com.simple.launcher.retirement.presentation.onboarding

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class OnboardingViewModel : BaseViewModel() {

    val action: StateFlow<ActionState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: android.graphics.Color.LTGRAY

        buildActionState(
            text = stringMap.getString(R.string.onboarding_start),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())
}
