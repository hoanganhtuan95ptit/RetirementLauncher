package com.simple.launcher.retirement.presentation.onboarding

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.text.emptyText
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import com.simple.launcher.retirement.utils.text.withStyleTitleLarge
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.toBigImage
import com.simple.ui.precompute.text.*
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import kotlinx.coroutines.flow.StateFlow

class OnboardingViewModel : BaseViewModel() {

    val title: StateFlow<BigText> = combineState(
        flow1 = resources,
        initialValue = emptyText()
    ) { resources ->

        value = resources.getString(R.string.onboarding_title)
            .withStyleTitleLarge()
            .with(BigBold, BigForegroundColor(resources.textColorPrimary))
            .build()
    }

    val description: StateFlow<BigText> = combineState(
        flow1 = resources,
        initialValue = emptyText()
    ) { resources ->

        value = resources.getString(R.string.onboarding_desc)
            .withStyleBodyLarge()
            .with(BigForegroundColor(resources.textColorSecondary))
            .build()
    }

    val image: StateFlow<BigImage> = combineState(
        flow1 = resources,
        initialValue = R.mipmap.ic_launcher.toBigImage()
    ) { _ ->

        value = R.mipmap.ic_launcher.toBigImage()
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary

        value = buildActionState(
            text = resources.getString(R.string.onboarding_start),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
