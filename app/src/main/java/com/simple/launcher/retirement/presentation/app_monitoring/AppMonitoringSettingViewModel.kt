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
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.coroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppMonitoringSettingViewModel : BaseViewModel() {

    val refresh = MutableStateFlow<Long>(0)

    val appBlockEnabledFlow = combineState(
        refresh,
        PreferenceRepository.instance.appBlockEnabledFlow(),
        false
    ) { _, isEnabled ->

        value = isEnabled && hasAppBlockPermissions()
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

        value = GroupViewItem(order = 1.2, list = list)
    }

    init {

        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

            val pendingEnabled = PreferenceRepository.instance.getPendingAppBlockEnabled() ?: return@launch
            SetAppMonitoringEnabledUseCase.instance(pendingEnabled)
        }
    }

    fun refreshStatus() {

        refresh.value = System.currentTimeMillis()
    }

    fun setAppBlockEnabled(isEnabled: Boolean) = viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

        PreferenceRepository.instance.setPendingAppBlockEnabled(isEnabled)
        SetAppMonitoringEnabledUseCase.instance(isEnabled)
    }

    private fun hasAppBlockPermissions(): Boolean {

        return PermissionRepository.instance.hasUsageStatsPermission() &&
                PermissionRepository.instance.hasOverlayPermission() &&
                PermissionRepository.instance.isDefaultLauncher()
    }
}
