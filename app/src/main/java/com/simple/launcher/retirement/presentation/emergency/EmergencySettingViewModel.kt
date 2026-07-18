package com.simple.launcher.retirement.presentation.emergency

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.StateFlow

class EmergencySettingViewModel : BaseViewModel() {

    val emergencyCallEnabledFlow = PreferenceRepository.instance.emergencyCallEnabledFlow()

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        emergencyCallEnabledFlow,
        null
    ) { resources, isEnabled ->

        value = GroupViewItem(
            order = 1.2,
            list = listOf(
                settingItem(
                    id = SettingItem.ID_EMERGENCY_CALL_TOGGLE,
                    icon = R.drawable.ic_sos_black_24dp,
                    title = R.string.setting_emergency_call,
                    isSwitch = true,
                    isChecked = isEnabled,
                    resources = resources
                )
            )
        )
    }
}
