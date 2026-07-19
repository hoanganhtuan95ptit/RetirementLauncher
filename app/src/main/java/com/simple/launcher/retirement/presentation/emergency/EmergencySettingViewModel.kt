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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class EmergencySettingViewModel : BaseViewModel() {

    private val preferenceRepository = PreferenceRepository.instance

    private val permissionRepository = PermissionRepository.instance

    private val setEmergencyCallEnabledUseCase = SetEmergencyCallEnabledUseCase.instance

    val emergencyCallEnabledFlow = preferenceRepository.emergencyCallEnabledFlow()
        .map { isEnabled ->

            isEnabled && hasEmergencyCallPermissions()
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

            val pendingConfig = preferenceRepository.getPendingEmergencyConfig() ?: return@launch
            setEmergencyCallEnabledUseCase(pendingConfig)
        }
    }

    private fun buildEmergencySettingGroup(resources: Map<String, Any>, isEnabled: Boolean): GroupViewItem {

        // Nhóm này được bơm vào tab Protect của Settings, cùng pattern với các setting service khác.
        return GroupViewItem(
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

    private fun hasEmergencyCallPermissions(): Boolean {

        return permissionRepository.hasCallPermission() &&
                permissionRepository.hasUserActivityAccessibilityPermission() &&
                permissionRepository.isDefaultLauncher()
    }
}
