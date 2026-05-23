package com.simple.launcher.retirement.presentation.pin_setup

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.StateFlow

data class PinVerifyContent(
    val title: RichText,
    val desc: RichText
)

class PinVerifyViewModel : BaseViewModel() {

    val content: StateFlow<PinVerifyContent> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = PinVerifyContent("".toRich(), "".toRich())
    ) { stringMap, themeMap ->
        val titleColor = themeMap.getColor(android.R.attr.textColorPrimary)
        val descColor = themeMap.getColor(android.R.attr.textColorSecondary)
        PinVerifyContent(
            title = stringMap.getString(R.string.pin_verify_title).toRich().with(ForegroundColor(titleColor)),
            desc = stringMap.getString(R.string.pin_verify_desc).toRich().with(ForegroundColor(descColor))
        )
    }
}
