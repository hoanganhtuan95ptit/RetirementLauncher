package com.simple.launcher.retirement.presentation.main.services.app_monitoring

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppMonitoringSettingViewModel : BaseViewModel() {

    val refreshTrigger = MutableStateFlow(0)

    val isAppBlockEnabledFlow = PreferenceRepository.instance.isAppBlockEnabledFlow()

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        refreshTrigger,
        isAppBlockEnabledFlow,
        null
    ) { resources, _, isEnabled ->

        value = settingItem(
            id = SettingItem.ID_TOGGLE_BLOCK,
            icon = R.drawable.ic_lock_black_24dp,
            title = R.string.setting_app_monitoring,

            isSwitch = true,
            isChecked = isEnabled,

            resources = resources
        ).let {

            GroupViewItem(order = 1.1, list = listOf(it))
        }
    }

    fun refresh() {

        refreshTrigger.value++
    }
}
