package com.simple.launcher.retirement.presentation.pin_setup

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.emptyText
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
        initialValue = PinVerifyContent(emptyText(), emptyText())
    ) { stringMap, themeMap ->
        val titleColor = themeMap.getColor(android.R.attr.textColorPrimary)
        val descColor = themeMap.getColor(android.R.attr.textColorSecondary)
        PinVerifyContent(
            title = RichText.Builder(stringMap.getString(R.string.pin_verify_title))
                .with(ForegroundColor(titleColor))
                .build(),
            desc = RichText.Builder(stringMap.getString(R.string.pin_verify_desc))
                .with(ForegroundColor(descColor))
                .build()
        )
    }
}
