package com.simple.launcher.retirement.presentation.settings.services.protect

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingHeader
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.StateFlow

class ProtectSettingViewModel : BaseViewModel() {

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        PreferenceRepository.instance.hasPinFlow(),
        PreferenceRepository.instance.isEmergencyCallEnabledFlow(),
        null
    ) { resources, hasPin, isEmergencyCallEnabled ->

        value = buildProtectGroup(
            resources = resources,
            hasPin = hasPin,
            isEmergencyCallEnabled = isEmergencyCallEnabled
        )
    }

    private fun buildProtectGroup(
        resources: Map<String, Any>,
        hasPin: Boolean,
        isEmergencyCallEnabled: Boolean
    ): GroupViewItem {

//        val items = buildList {
//
//            settingHeader(
//                title = R.string.setting_header_security,
//                resources = resources
//            ).let {
//
//                add(it)
//            }
//
//            settingItem(
//                id = SettingItem.ID_EMERGENCY_CALL_TOGGLE,
//                icon = R.drawable.ic_sos_black_24dp,
//                title = R.string.setting_emergency_call,
//                isSwitch = true,
//                isChecked = isEmergencyCallEnabled,
//                resources = resources
//            ).let {
//
//                add(it)
//            }
//
//        }

        return GroupViewItem(order = 1, list = emptyList())
    }
}
