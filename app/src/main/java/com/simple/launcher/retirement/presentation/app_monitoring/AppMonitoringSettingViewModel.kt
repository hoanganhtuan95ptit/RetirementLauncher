package com.simple.launcher.retirement.presentation.app_monitoring

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class AppMonitoringSettingViewModel : BaseViewModel() {

    val isHasAppFlow = AppRepository.instance.homeDataFlow().map {
        AppRepository.instance.getSelectedPackages().isNotEmpty()
    }.flowOn(Dispatchers.Default)

    val isAppBlockEnabledFlow = PreferenceRepository.instance.isAppBlockEnabledFlow()

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        isHasAppFlow,
        isAppBlockEnabledFlow,
        null
    ) { resources, isHasApp, isEnabled ->

        val list = arrayListOf<ViewItem>()

        if (isHasApp) settingItem(
            id = SettingItem.ID_TOGGLE_BLOCK,
            icon = R.drawable.ic_lock_black_24dp,
            title = R.string.setting_app_monitoring,

            isSwitch = true,
            isChecked = isEnabled,

            resources = resources
        ).let {

            list.add(it)
        }

        value = GroupViewItem(order = 1.1, list = list)
    }
}
