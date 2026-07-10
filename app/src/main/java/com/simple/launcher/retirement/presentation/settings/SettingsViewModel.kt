package com.simple.launcher.retirement.presentation.settings

import androidx.lifecycle.viewModelScope
import com.simple.adapter.ViewItem
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.ViewItemViewModel
import com.simple.launcher.retirement.presentation.base.adapters.SpaceViewItem
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.size.navigationBarHeight
import com.simple.launcher.retirement.utils.size.width
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewItemViewModel() {

    val toolbar: StateFlow<ToolbarState> = combineState(
        resources,
        ToolbarState.empty()
    ) { resources ->

        val color = resources.textColorPrimary
        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.settings_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val bottomViewItem: StateFlow<List<ViewItem>> = combineState(
        resources,
        emptyList()
    ) { resources ->

        value = SpaceViewItem(
            width = resources.width,
            height = resources.navigationBarHeight + 24.dp().toInt(),

            span = 2
        ).apply {
            buildDrawSpec(resources)
        }.let {
            listOf(it)
        }
    }

    init {
        bottomViewItem.launchCollect(viewModelScope) {

            updateItem(Double.MAX_VALUE, it)
        }
    }
}
