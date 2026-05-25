package com.simple.launcher.retirement.presentation.main.services.file_cleanup

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileCleanupSettingViewModel : BaseViewModel() {

    private val repository = PreferenceRepository.instance

    val refreshTrigger = MutableStateFlow(0)

    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = repository.isFileCleanupEnabledFlow(),
        flow4 = refreshTrigger,
        initialValue = emptyList()
    ) { stringMap, themeMap, isEnabled, _ ->

        val textColor = themeMap.getColor(android.R.attr.textColorPrimary)

        listOf(
            SettingItem(
                SettingItem.ID_TOGGLE_CLEANUP,
                stringMap.getString(R.string.setting_auto_cleanup_apk).let { RichText.Builder(it).with(ForegroundColor(textColor)).build() },
                ImageRes(android.R.drawable.ic_menu_save),
                isSwitch = true,
                isChecked = isEnabled
            )
        )
    }

    fun refresh() = refreshTrigger.value++
}
