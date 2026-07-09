package com.simple.launcher.retirement.presentation.settings.services.common

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingHeader
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.StateFlow

class CommonSettingViewModel : BaseViewModel() {

    val viewItemList: StateFlow<GroupViewItem?> = combineState(resources, null) { resources ->

        buildCommonGroup(resources)
    }

    private fun buildCommonGroup(resources: Map<String, Any>): GroupViewItem {

        val items = buildList {

            add(
                settingHeader(
                    title = R.string.setting_header_general,
                    resources = resources
                )
            )

            if (!PermissionManager.isDefaultLauncher()) {

                add(
                    settingItem(
                        id = SettingItem.ID_DEFAULT_LAUNCHER,
                        icon = android.R.drawable.ic_menu_manage,
                        title = R.string.setting_default_launcher,
                        resources = resources
                    )
                )
            }

            add(
                settingItem(
                    id = SettingItem.ID_APP_LIST,
                    icon = android.R.drawable.ic_menu_agenda,
                    title = R.string.setting_app_list,
                    resources = resources
                )
            )

            add(
                settingItem(
                    id = SettingItem.ID_CONTACT_LIST,
                    icon = android.R.drawable.ic_menu_call,
                    title = R.string.setting_contact_list,
                    resources = resources
                )
            )
        }

        return GroupViewItem(order = 0, list = items)
    }
}
