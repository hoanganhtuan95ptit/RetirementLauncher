package com.simple.launcher.retirement.presentation.onboarding

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.getColor
import kotlinx.coroutines.flow.StateFlow

class OnboardingViewModel : BaseViewModel() {

    val action: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.getColor(com.google.android.material.R.attr.colorOnPrimary)
        val backgroundColor = resources.getColor(android.R.attr.colorPrimary, android.graphics.Color.LTGRAY)

        buildActionState(
            text = resources.getString(R.string.onboarding_start),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
