package com.simple.launcher.retirement.presentation.notification_block

import androidx.lifecycle.viewModelScope
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.domain.usecase.SetNotificationBlockEnabledUseCase
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

class NotificationBlockSettingViewModel : BaseViewModel() {

    val refresh = MutableStateFlow<Long>(0)

    val notificationBlockEnabledFlow = combineState(
        refresh,
        PreferenceRepository.instance.notificationBlockEnabledFlow(),
        false
    ) { _, isEnabled ->

        value = isEnabled && hasNotificationBlockPermissions()
    }

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        notificationBlockEnabledFlow,
        null
    ) { resources, isEnabled ->

        val list = arrayListOf<ViewItem>()

        settingItem(
            id = SettingItem.ID_NOTIFICATION_BLOCK,
            icon = R.drawable.ic_notification_block_24dp,
            title = R.string.setting_notification_block,
            description = R.string.setting_notification_block_desc,

            isSwitch = true,
            isChecked = isEnabled,

            resources = resources
        ).let {

            list.add(it)
        }

        value = GroupViewItem(order = SettingItem.ORDER_NOTIFICATION_BLOCK, list = list)
    }

    init {

        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

            val pendingEnabled = PreferenceRepository.instance.getPendingNotificationBlockEnabled()
                ?: return@launch
            SetNotificationBlockEnabledUseCase.instance(pendingEnabled)
        }
    }

    fun refreshStatus() {

        refresh.value = System.currentTimeMillis()
    }

    fun setNotificationBlockEnabled(isEnabled: Boolean) = viewModelScope.launch(
        coroutineExceptionHandler + Dispatchers.Default
    ) {

        PreferenceRepository.instance.setPendingNotificationBlockEnabled(isEnabled)
        SetNotificationBlockEnabledUseCase.instance(isEnabled)
    }

    private fun hasNotificationBlockPermissions(): Boolean {

        return PermissionRepository.instance.hasNotificationListenerAccess()
    }
}
