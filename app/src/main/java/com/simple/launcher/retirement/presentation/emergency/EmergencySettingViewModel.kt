package com.simple.launcher.retirement.presentation.emergency

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.domain.usecase.SetEmergencyCallEnabledUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.coroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmergencySettingViewModel : BaseViewModel() {

    val refresh = MutableStateFlow<Long>(0)

    val emergencyCallEnabledFlow: SharedFlow<Boolean> = combineState(
        refresh,
        PreferenceRepository.instance.emergencyCallEnabledFlow(),
        false
    ) { _, isEnabled ->

        value = isEnabled && hasEmergencyCallPermissions()
    }

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        emergencyCallEnabledFlow,
        null
    ) { resources, isEnabled ->

        value = buildEmergencySettingGroup(resources, isEnabled)
    }

    init {

        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

            val pendingConfig = PreferenceRepository.instance.getPendingEmergencyConfig() ?: return@launch
            SetEmergencyCallEnabledUseCase.instance(pendingConfig)
        }
    }

    fun refreshStatus() {
        refresh.value = System.currentTimeMillis()
    }

    private fun buildEmergencySettingGroup(resources: Map<String, Any>, isEnabled: Boolean): GroupViewItem {

        val settingViewItem = settingItem(
            id = SettingItem.ID_EMERGENCY_CALL_TOGGLE,
            icon = R.drawable.ic_sos_black_24dp,
            title = R.string.setting_emergency_call,
            isSwitch = true,
            isChecked = isEnabled,
            resources = resources
        )

        // Nhóm này được bơm vào tab Protect của Settings, cùng pattern với các setting service khác.
        return GroupViewItem(
            order = 1.3,
            list = listOf(settingViewItem)
        )
    }

    private fun hasEmergencyCallPermissions(): Boolean {

        return PermissionRepository.instance.hasCallPermission() &&
                PermissionRepository.instance.hasUserActivityAccessibilityPermission() &&
                PermissionRepository.instance.isDefaultLauncher()
    }
}
