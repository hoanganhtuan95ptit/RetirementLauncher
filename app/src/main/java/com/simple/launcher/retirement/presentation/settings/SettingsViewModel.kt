package com.simple.launcher.retirement.presentation.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val repository: AppRepository
) : BaseViewModel() {

    val toolbar: StateFlow<ToolbarState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val title = buildToolbarTitle(stringMap.getString(R.string.settings_title), color)
        ToolbarState(title = title, backIcon = buildBackIcon(color))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ToolbarState.empty())

    private val _refreshTrigger = MutableStateFlow(0)

    val items: StateFlow<List<SettingItem>> = combine(strings, themes, _refreshTrigger) { stringMap, themeMap, _ ->
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val settingsItems = mutableListOf<SettingItem>()
        
        fun Int.toSettingRichText() = stringMap.getString(this).toRich().with(ForegroundColor(textColor))

        if (repository.hasPin()) {
            settingsItems.add(SettingItem(SettingItem.ID_PIN, R.string.setting_pin.toSettingRichText(), ImageRes(android.R.drawable.ic_lock_idle_lock)))
        }

        settingsItems.addAll(listOf(
            SettingItem(SettingItem.ID_APP_LIST, R.string.setting_app_list.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_agenda)),
            SettingItem(SettingItem.ID_DEFAULT_LAUNCHER, R.string.setting_default_launcher.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_manage)),
            SettingItem(SettingItem.ID_CONTACT_LIST, R.string.setting_contact_list.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_call)),
            SettingItem(SettingItem.ID_CLEAN_FILES, R.string.setting_clean_files.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_delete)),
            SettingItem(SettingItem.ID_CLEAN_MEMORY, R.string.setting_clean_memory.toSettingRichText(), ImageRes(android.R.drawable.ic_media_play)),
            SettingItem(SettingItem.ID_TOGGLE_BLOCK, R.string.setting_app_monitoring.toSettingRichText(), ImageRes(android.R.drawable.ic_lock_lock), true, repository.isAppBlockEnabled()),
            SettingItem(SettingItem.ID_TOGGLE_CLEANUP, R.string.setting_auto_cleanup_apk.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_save), true, repository.isFileCleanupEnabled())
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            settingsItems.add(SettingItem(SettingItem.ID_TOGGLE_CALL_BLOCK, R.string.setting_call_block.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_call), true, repository.isCallBlockEnabled()))
        }
        settingsItems
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun loadSettings() {
        _refreshTrigger.value++
    }
    
    fun updateItem(item: SettingItem) {
        _refreshTrigger.value++
    }
}

class SettingsViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository) as T
    }
}
