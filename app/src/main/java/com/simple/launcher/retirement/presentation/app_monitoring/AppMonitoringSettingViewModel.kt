package com.simple.launcher.retirement.presentation.app_monitoring

import androidx.lifecycle.viewModelScope
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.domain.usecase.SetAppMonitoringEnabledUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.coroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppMonitoringSettingViewModel : BaseViewModel() {

    private val preferenceRepository = PreferenceRepository.instance

    private val permissionRepository = PermissionRepository.instance

    private val setAppMonitoringEnabledUseCase = SetAppMonitoringEnabledUseCase.instance

    val appBlockEnabledFlow = preferenceRepository.appBlockEnabledFlow()
        .map { isEnabled ->

            isEnabled && hasAppBlockPermissions()
        }

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        appBlockEnabledFlow,
        null
    ) { resources, isEnabled ->

        val list = arrayListOf<ViewItem>()

        settingItem(
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

    init {

        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

            val pendingEnabled = preferenceRepository.getPendingAppBlockEnabled() ?: return@launch
            setAppMonitoringEnabledUseCase(pendingEnabled)
        }
    }

    fun setAppBlockEnabled(isEnabled: Boolean) = viewModelScope.launch(
        coroutineExceptionHandler + Dispatchers.Default
    ) {

        preferenceRepository.setPendingAppBlockEnabled(isEnabled)
        setAppMonitoringEnabledUseCase(isEnabled)
    }

    private fun hasAppBlockPermissions(): Boolean {

        return permissionRepository.hasUsageStatsPermission() &&
                permissionRepository.hasOverlayPermission() &&
                permissionRepository.isDefaultLauncher()
    }
}
