package com.simple.launcher.retirement.presentation.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.string.asStringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val repository: AppRepository
) : BaseViewModel() {

    private val _items = MutableStateFlow<List<SettingItem>>(emptyList())
    val items: StateFlow<List<SettingItem>> = _items.asStateFlow()

    fun loadSettings() {
        val settingsItems = mutableListOf<SettingItem>()
        if (repository.hasPin()) {
            settingsItems.add(SettingItem(SettingItem.ID_PIN, R.string.setting_pin.asStringRes(), ImageRes(android.R.drawable.ic_lock_idle_lock)))
        }

        settingsItems.addAll(listOf(
            SettingItem(SettingItem.ID_APP_LIST, R.string.setting_app_list.asStringRes(), ImageRes(android.R.drawable.ic_menu_agenda)),
            SettingItem(SettingItem.ID_DEFAULT_LAUNCHER, R.string.setting_default_launcher.asStringRes(), ImageRes(android.R.drawable.ic_menu_manage)),
            SettingItem(SettingItem.ID_CONTACT_LIST, R.string.setting_contact_list.asStringRes(), ImageRes(android.R.drawable.ic_menu_call)),
            SettingItem(SettingItem.ID_CLEAN_FILES, R.string.setting_clean_files.asStringRes(), ImageRes(android.R.drawable.ic_menu_delete)),
            SettingItem(SettingItem.ID_CLEAN_MEMORY, R.string.setting_clean_memory.asStringRes(), ImageRes(android.R.drawable.ic_media_play)),
            SettingItem(SettingItem.ID_TOGGLE_BLOCK, R.string.setting_app_monitoring.asStringRes(), ImageRes(android.R.drawable.ic_lock_lock), true, repository.isAppBlockEnabled()),
            SettingItem(SettingItem.ID_TOGGLE_CLEANUP, R.string.setting_auto_cleanup_apk.asStringRes(), ImageRes(android.R.drawable.ic_menu_save), true, repository.isFileCleanupEnabled())
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            settingsItems.add(SettingItem(SettingItem.ID_TOGGLE_CALL_BLOCK, R.string.setting_call_block.asStringRes(), ImageRes(android.R.drawable.ic_menu_call), true, repository.isCallBlockEnabled()))
        }
        _items.value = settingsItems
    }
    
    fun updateItem(item: SettingItem) {
        val currentList = _items.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            currentList[index] = item.copy()
            _items.value = currentList
        }
    }
}

class SettingsViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository) as T
    }
}
