package com.simple.launcher.retirement.presentation.installer_cleanup

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.StateFlow

class InstallerCleanupSettingViewModel : BaseViewModel() {

    val fileCleanupEnabledFlow = PreferenceRepository.instance.fileCleanupEnabledFlow()

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        fileCleanupEnabledFlow,
        null
    ) { resources, isEnabled ->

        val list = arrayListOf<ViewItem>()

        settingItem(
            id = SettingItem.ID_TOGGLE_INSTALLER_CLEANUP,
            icon = android.R.drawable.ic_menu_save,
            title = R.string.setting_auto_cleanup_apk,

            isSwitch = true,
            isChecked = isEnabled,

            resources = resources
        ).let {

            list.add(it)
        }

//        value = GroupViewItem(order = 1.3, list = list)
    }
}
