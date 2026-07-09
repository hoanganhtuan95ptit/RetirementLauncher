package com.simple.launcher.retirement.presentation.main.services.file_cleanup

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.text.*
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileCleanupSettingViewModel : BaseViewModel() {

    private val repository = PreferenceRepository.instance

    val refreshTrigger = MutableStateFlow(0)

    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = resources,
        flow2 = repository.isFileCleanupEnabledFlow(),
        flow3 = refreshTrigger,
        initialValue = emptyList()
    ) { resources, isEnabled, _ ->

        val textColor = resources.textColorPrimary

        value = listOf(
            SettingItem(
                SettingItem.ID_TOGGLE_CLEANUP,
                resources.getString(R.string.setting_auto_cleanup_apk).with(ForegroundColor(textColor)).build(),
                ImageRes(android.R.drawable.ic_menu_save),
                isSwitch = true,
                isChecked = isEnabled
            )
        )
    }

    fun refresh() = refreshTrigger.value++
}
