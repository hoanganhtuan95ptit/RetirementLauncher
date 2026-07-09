package com.simple.launcher.retirement.presentation.settings.services.protect

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingHeader
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.StateFlow

class ProtectSettingViewModel : BaseViewModel() {

    private val preferenceRepository by lazy { PreferenceRepository.instance }

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        flow2 = preferenceRepository.hasPinFlow(),
        flow3 = preferenceRepository.isEmergencyCallEnabledFlow(),
        initialValue = null
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

        val items = buildList {

            add(
                settingHeader(
                    title = R.string.setting_header_security,
                    resources = resources
                )
            )

            add(
                settingItem(
                    id = SettingItem.ID_EMERGENCY_CALL_TOGGLE,
                    icon = android.R.drawable.ic_menu_manage,
                    title = R.string.setting_emergency_call,
                    isSwitch = true,
                    isChecked = isEmergencyCallEnabled,
                    resources = resources
                )
            )

            if (hasPin) {

                add(
                    settingItem(
                        id = SettingItem.ID_PIN,
                        icon = android.R.drawable.ic_lock_idle_lock,
                        title = R.string.setting_pin,
                        resources = resources
                    )
                )
            }
        }

        return GroupViewItem(order = 1, list = items)
    }
}
