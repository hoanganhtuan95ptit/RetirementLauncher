package com.simple.launcher.retirement.presentation.settings.services.common

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingHeader
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.StateFlow

class CommonSettingViewModel : BaseViewModel() {

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        PreferenceRepository.instance.hasPinFlow(),
        null
    ) { resources, hasPin ->

        val items = arrayListOf<ViewItem>()

        settingHeader(
            title = R.string.setting_header_general,
            resources = resources
        ).let {

            items.add(it)
        }

        if (!PermissionManager.isDefaultLauncher()) settingItem(
            id = SettingItem.ID_DEFAULT_LAUNCHER,
            icon = android.R.drawable.ic_menu_manage,
            title = R.string.setting_default_launcher,
            resources = resources
        ).let {

            items.add(it)
        }

        settingItem(
            id = SettingItem.ID_APP_LIST,
            icon = R.drawable.ic_apps_black_24dp,
            title = R.string.setting_app_list,
            resources = resources
        ).let {

            items.add(it)
        }

        settingItem(
            id = SettingItem.ID_CONTACT_LIST,
            icon = R.drawable.ic_call_calling_black_24dp,
            title = R.string.setting_contact_list,
            resources = resources
        ).let {

            items.add(it)
        }

        if (hasPin) settingItem(
            id = SettingItem.ID_PIN,
            icon = R.drawable.ic_pin_black_24dp,
            title = R.string.setting_pin,
            resources = resources
        ).let {

            items.add(it)
        }

        value = GroupViewItem(order = 0, list = items)
    }
}
