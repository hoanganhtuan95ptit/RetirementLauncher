package com.simple.launcher.retirement.presentation.settings

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.ViewItemViewModel
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewItemViewModel() {

    val toolbar: StateFlow<ToolbarState> = combineState(flow1 = resources, initialValue = ToolbarState.empty()) { resources ->

        val color = resources.textColorPrimary
        ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.settings_title), color),
            backIcon = buildBackIcon(color)
        )
    }
}
