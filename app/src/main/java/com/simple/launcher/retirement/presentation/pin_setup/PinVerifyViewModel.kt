package com.simple.launcher.retirement.presentation.pin_setup

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.text.*
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
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
        val titleColor = resources.textColorPrimary
        val descColor = resources.textColorSecondary
        value = PinVerifyContent(
            title = resources.getString(R.string.pin_verify_title)
                .with(ForegroundColor(titleColor))
                .build(),
            desc = resources.getString(R.string.pin_verify_desc)
                .with(ForegroundColor(descColor))
                .build()
        )
    }
}
