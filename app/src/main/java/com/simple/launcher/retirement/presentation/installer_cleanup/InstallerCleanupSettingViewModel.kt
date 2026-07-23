package com.simple.launcher.retirement.presentation.installer_cleanup

import androidx.lifecycle.viewModelScope
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.domain.usecase.SetFileCleanupEnabledUseCase
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

class InstallerCleanupSettingViewModel : BaseViewModel() {

    private val preferenceRepository = PreferenceRepository.instance

    private val permissionRepository = PermissionRepository.instance

    private val setFileCleanupEnabledUseCase = SetFileCleanupEnabledUseCase.instance

    val fileCleanupEnabledFlow = preferenceRepository.fileCleanupEnabledFlow()
        .map { isEnabled ->

            isEnabled && hasFileCleanupPermissions()
        }

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        fileCleanupEnabledFlow,
        null
    ) { resources, isEnabled ->

        val list = arrayListOf<ViewItem>()

        settingItem(
            id = SettingItem.ID_TOGGLE_INSTALLER_CLEANUP,
            icon = R.drawable.ic_protect_black_24dp,
            title = R.string.setting_protection_feature,
            description = R.string.setting_protection_feature_desc,

            isSwitch = true,
            isChecked = isEnabled,
            highlight = true,

            resources = resources
        ).let {

            list.add(it)
        }

        value = GroupViewItem(order = 1.01, list = list)
    }

    init {

        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

            val pendingEnabled = preferenceRepository.getPendingFileCleanupEnabled() ?: return@launch
            setFileCleanupEnabledUseCase(pendingEnabled)
        }
    }

    fun setFileCleanupEnabled(isEnabled: Boolean) = viewModelScope.launch(
        coroutineExceptionHandler + Dispatchers.Default
    ) {

        preferenceRepository.setPendingFileCleanupEnabled(isEnabled)
        setFileCleanupEnabledUseCase(isEnabled)
    }

    private fun hasFileCleanupPermissions(): Boolean {

        return permissionRepository.hasFilePermission()
    }
}
