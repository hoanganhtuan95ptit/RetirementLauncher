package com.simple.launcher.retirement.presentation.emergency

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.StateFlow

class EmergencySettingViewModel : BaseViewModel() {

    val isHasContactFlow = combineState(
        ContactRepository.instance.homeDataFlow(),
        false,
    ) {

        value = ContactRepository.instance.getSelectedContacts().isNotEmpty()
    }

    val isEmergencyCallEnabledFlow = PreferenceRepository.instance.isEmergencyCallEnabledFlow()

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        isHasContactFlow,
        isEmergencyCallEnabledFlow,
        null
    ) { resources, isHasContact, isEnabled ->

        val list = arrayListOf<ViewItem>()

        if (isHasContact) settingItem(
            id = SettingItem.ID_EMERGENCY_CALL_TOGGLE,
            icon = R.drawable.ic_sos_black_24dp,
            title = R.string.setting_emergency_call,

            isSwitch = true,
            isChecked = isEnabled,

            resources = resources
        ).let {

            list.add(it)
        }

        value = GroupViewItem(order = 1.2, list = list)
    }
}
