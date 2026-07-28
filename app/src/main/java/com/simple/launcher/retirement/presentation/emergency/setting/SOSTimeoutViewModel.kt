package com.simple.launcher.retirement.presentation.emergency.setting

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.withStyleTitleLarge
import com.simple.launcher.retirement.utils.exts.withStyleTitleMedium
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.StateFlow

class SOSTimeoutViewModel : BaseViewModel() {

    val title: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.sos_timeout_label)
            .withStyleTitleLarge()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val confirmLabel: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = android.graphics.Color.WHITE
        value = resources.getString(R.string.sos_save_changes)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val horizontalPadding: StateFlow<Int> = combineState(resources, 16.dp()) {

        value = 16.dp()
    }

    val bottomPadding: StateFlow<Int> = combineState(resources, 24.dp()) {

        value = 24.dp()
    }

    val listMarginBottom: StateFlow<Int> = combineState(resources, 16.dp()) {

        value = 16.dp()
    }
}
