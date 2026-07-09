package com.simple.launcher.retirement.presentation.main.services.call_block

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CallBlockSettingViewModel : BaseViewModel() {

    private val repository = PreferenceRepository.instance

    val refreshTrigger = MutableStateFlow(0)

    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = resources,
        flow2 = repository.isCallBlockEnabledFlow(),
        flow3 = refreshTrigger,
        initialValue = emptyList()
    ) { resources, isEnabled, _ ->

        val textColor = resources.textColorPrimary

        value = listOf(
            SettingItem(
                SettingItem.ID_TOGGLE_CALL_BLOCK,
                resources.getString(R.string.setting_call_block).with(BigForegroundColor(textColor)).build(),
                BigImage(android.R.drawable.ic_menu_call),
                isSwitch = true,
                isChecked = isEnabled
            )
        )
    }

    fun refresh() = refreshTrigger.value++
}
