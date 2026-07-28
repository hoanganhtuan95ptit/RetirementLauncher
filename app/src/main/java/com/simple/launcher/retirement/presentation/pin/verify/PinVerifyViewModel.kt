package com.simple.launcher.retirement.presentation.pin.verify

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.exts.withStyleTitleLarge
import com.simple.launcher.retirement.utils.exts.withStyleTitleMedium
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.emptyText
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.StateFlow

data class PinVerifyContent(
    val title: BigText,
    val desc: BigText
)

class PinVerifyViewModel : BaseViewModel() {

    val content: StateFlow<PinVerifyContent> = combineState(
        flow1 = resources,
        initialValue = PinVerifyContent(emptyText(), emptyText())
    ) { resources ->

        val titleColor = resources.textColorPrimary
        val descColor = resources.textColorSecondary

        value = PinVerifyContent(
            title = resources.getString(R.string.pin_verify_title)
                .withStyleTitleLarge()
                .with(BigForegroundColor(titleColor))
                .build(),
            desc = resources.getString(R.string.pin_verify_desc)
                .withStyleTitleMedium()
                .with(BigForegroundColor(descColor))
                .build()
        )
    }
}
