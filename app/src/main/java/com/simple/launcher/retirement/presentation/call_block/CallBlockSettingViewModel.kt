package com.simple.launcher.retirement.presentation.call_block

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.StateFlow

class CallBlockSettingViewModel : BaseViewModel() {

    private val repository = PreferenceRepository.instance

    val isCallBlockEnabledFlow = repository.isCallBlockEnabledFlow()

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        isCallBlockEnabledFlow,
        null
    ) { resources, isEnabled ->

        val list = arrayListOf<ViewItem>()

        settingItem(
            id = SettingItem.ID_TOGGLE_CALL_BLOCK,
            icon = android.R.drawable.ic_menu_call,
            title = R.string.setting_call_block,

            isSwitch = true,
            isChecked = isEnabled,

            resources = resources
        ).let {

            list.add(it)
        }

        value = GroupViewItem(order = 1.4, list = list)
    }
}
