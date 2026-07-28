package com.simple.launcher.retirement.presentation.clock

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.exts.combineState
import kotlinx.coroutines.flow.StateFlow

class ClockSettingViewModel : BaseViewModel() {

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        null
    ) { resources ->

        val items = buildList {

            add(
                settingItem(
                    id = SettingItem.ID_LUNAR_CALENDAR_TOGGLE,
                    icon = R.drawable.ic_apps_black_24dp,
                    title = R.string.setting_clock_title,
                    isSwitch = false,
                    isChecked = false,
                    resources = resources
                )
            )
        }

        value = GroupViewItem(order = 1.0, list = items)
    }
}
