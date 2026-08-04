package com.simple.launcher.retirement.presentation.settings.services.protect

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.exts.combineState
import kotlinx.coroutines.flow.StateFlow

class NotificationBlockSettingViewModel : BaseViewModel() {

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        PreferenceRepository.instance.notificationBlockEnabledFlow(),
        null
    ) { resources, isEnabled ->

        val items = listOf(
            settingItem(
                id = SettingItem.ID_NOTIFICATION_BLOCK,
                icon = R.drawable.ic_notification_block_24dp,
                title = R.string.setting_notification_block,
                description = R.string.setting_notification_block_desc,
                isSwitch = true,
                isChecked = isEnabled,
                resources = resources
            )
        )

        value = GroupViewItem(order = SettingItem.ORDER_NOTIFICATION_BLOCK, list = items)
    }
}
