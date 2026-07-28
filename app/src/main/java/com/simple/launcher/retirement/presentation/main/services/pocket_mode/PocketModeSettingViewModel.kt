package com.simple.launcher.retirement.presentation.main.services.pocket_mode

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PocketModeSettingViewModel : BaseViewModel() {

    private val repository by lazy { PreferenceRepository.instance }

    val refreshTrigger = MutableStateFlow(0)

    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = resources,
        flow2 = repository.pocketModeEnabledFlow(),
        flow3 = refreshTrigger,
        initialValue = emptyList()
    ) { resources, isEnabled, _ ->

        val textColor = resources.textColorPrimary

        value = listOf(
            SettingItem(
                id = SettingItem.ID_TOGGLE_POCKET_MODE,
                title = resources.getString(R.string.setting_pocket_mode).with(BigForegroundColor(textColor)).build(),
                icon = BigImage(android.R.drawable.ic_menu_compass),
                isSwitch = true,
                isChecked = isEnabled
            )
        )
    }

    fun refresh() = refreshTrigger.value++
}
