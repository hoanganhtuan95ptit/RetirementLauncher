package com.simple.launcher.retirement.presentation.settings.services.plugins

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.exts.combineState
import kotlinx.coroutines.flow.StateFlow

class UninstallSettingViewModel : BaseViewModel() {

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        null
    ) { resources ->

        val items = buildList {

            add(
                settingItem(
                    id = SettingItem.ID_UNINSTALL_APPS,
                    icon = R.drawable.ic_delete_black_24dp,
                    title = R.string.setting_uninstall_apps,
                    resources = resources
                )
            )
        }

        value = GroupViewItem(order = 2.0, list = items)
    }
}
