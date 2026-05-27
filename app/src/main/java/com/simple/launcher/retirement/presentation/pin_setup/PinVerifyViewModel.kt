package com.simple.launcher.retirement.presentation.pin_setup

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.text.*
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.getColor
import kotlinx.coroutines.flow.StateFlow

data class PinVerifyContent(
    val title: RichText,
    val desc: RichText
)

class PinVerifyViewModel : BaseViewModel() {

    val content: StateFlow<PinVerifyContent> = combineState(
        flow1 = resources,
        initialValue = PinVerifyContent(emptyText(), emptyText())
    ) { resources ->
        val titleColor = resources.getColor(android.R.attr.textColorPrimary)
        val descColor = resources.getColor(android.R.attr.textColorSecondary)
        PinVerifyContent(
            title = resources.getString(R.string.pin_verify_title)
                .with(ForegroundColor(titleColor))
                .build(),
            desc = resources.getString(R.string.pin_verify_desc)
                .with(ForegroundColor(descColor))
                .build()
        )
    }
}
