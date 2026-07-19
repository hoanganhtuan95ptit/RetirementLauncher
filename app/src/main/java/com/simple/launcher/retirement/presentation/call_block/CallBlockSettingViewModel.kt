package com.simple.launcher.retirement.presentation.call_block

import androidx.lifecycle.viewModelScope
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.domain.usecase.SetCallBlockEnabledUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.coroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CallBlockSettingViewModel : BaseViewModel() {

    private val preferenceRepository = PreferenceRepository.instance

    private val setCallBlockEnabledUseCase = SetCallBlockEnabledUseCase.instance

    val callBlockEnabledFlow = preferenceRepository.callBlockEnabledFlow()

    val viewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        callBlockEnabledFlow,
        null
    ) { resources, isEnabled ->

        val list = arrayListOf<ViewItem>()

        settingItem(
            id = SettingItem.ID_TOGGLE_CALL_BLOCK,
            icon = android.R.drawable.ic_menu_call,
            title = R.string.setting_call_block,

            isSwitch = true,
            isChecked = isEnabled,

            resources = resources
        ).let {

            list.add(it)
        }

        value = GroupViewItem(order = 1.4, list = list)
    }

    init {

        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

            val pendingEnabled = preferenceRepository.getPendingCallBlockEnabled() ?: return@launch
            setCallBlockEnabledUseCase(pendingEnabled)
        }
    }

    fun setCallBlockEnabled(isEnabled: Boolean) = viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

        preferenceRepository.setPendingCallBlockEnabled(isEnabled)
        setCallBlockEnabledUseCase(isEnabled)
    }
}
