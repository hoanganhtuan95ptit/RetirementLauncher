package com.simple.launcher.retirement.presentation.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/**
 * Mô tả vị trí muốn chèn một SettingItem vào danh sách.
 */
sealed class InsertPosition {
    data class AtIndex(val index: Int) : InsertPosition()
    data class AfterItem(val anchorId: Int) : InsertPosition()
    object AtEnd : InsertPosition()
}

/**
 * Gói một [SettingItem] cùng với [InsertPosition] mô tả nơi item đó sẽ được chèn.
 */
data class PositionedItem(
    val position: InsertPosition,
    val item: SettingItem
)

class SettingsViewModel() : BaseViewModel() {

    private val repository by lazy {
        PreferenceRepository.instance
    }

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ToolbarState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(R.string.settings_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    private val _refreshTrigger = MutableStateFlow(0)
    private val _extraItems = MutableStateFlow<List<PositionedItem>>(emptyList())

    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = combine(
            repository.isAppBlockEnabledFlow(),
            repository.isFileCleanupEnabledFlow(),
            repository.isCallBlockEnabledFlow(),
            repository.hasPinFlow(),
            _extraItems,
            _refreshTrigger
        ) { args ->
            SettingsState(
                appBlockEnabled = args[0] as Boolean,
                fileCleanupEnabled = args[1] as Boolean,
                callBlockEnabled = args[2] as Boolean,
                hasPin = args[3] as Boolean,
                extraItems = args[4] as List<PositionedItem>,
                refreshTrigger = args[5] as Int
            )
        },
        initialValue = emptyList()
    ) { stringMap, themeMap, state ->
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary)
        val settingsItems = mutableListOf<ViewItem>()

        fun Int.toSettingRichText() = stringMap.getString(this).toRich().with(ForegroundColor(textColor))

        // ── Danh sách gốc ────────────────────────────────────────────────────
        if (state.hasPin) {
            settingsItems.add(SettingItem(SettingItem.ID_PIN, R.string.setting_pin.toSettingRichText(), ImageRes(android.R.drawable.ic_lock_idle_lock)))
        }

        settingsItems.addAll(listOf(
            SettingItem(SettingItem.ID_DEFAULT_LAUNCHER, R.string.setting_default_launcher.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_manage)),
            SettingItem(SettingItem.ID_APP_LIST, R.string.setting_app_list.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_agenda)),
            SettingItem(SettingItem.ID_CONTACT_LIST, R.string.setting_contact_list.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_call)),
            SettingItem(SettingItem.ID_CLEAN_FILES, R.string.setting_clean_files.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_delete)),
            SettingItem(SettingItem.ID_CLEAN_MEMORY, R.string.setting_clean_memory.toSettingRichText(), ImageRes(android.R.drawable.ic_media_play)),
            SettingItem(SettingItem.ID_TOGGLE_BLOCK, R.string.setting_app_monitoring.toSettingRichText(), ImageRes(android.R.drawable.ic_lock_lock), true, state.appBlockEnabled),
            SettingItem(SettingItem.ID_TOGGLE_CLEANUP, R.string.setting_auto_cleanup_apk.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_save), true, state.fileCleanupEnabled)
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            settingsItems.add(SettingItem(SettingItem.ID_TOGGLE_CALL_BLOCK, R.string.setting_call_block.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_call), true, state.callBlockEnabled))
        }

        // ── Chèn extra items theo vị trí ─────────────────────────────────────
        state.extraItems.filter { it.position is InsertPosition.AtIndex }
            .sortedBy { (it.position as InsertPosition.AtIndex).index }
            .forEach { positioned ->
                val idx = (positioned.position as InsertPosition.AtIndex).index
                    .coerceIn(0, settingsItems.size)
                settingsItems.add(idx, positioned.item)
            }

        state.extraItems.filter { it.position is InsertPosition.AfterItem }
            .forEach { positioned ->
                val anchorId = (positioned.position as InsertPosition.AfterItem).anchorId
                val anchorIdx = settingsItems.indexOfFirst {
                    it is SettingItem && it.id == anchorId
                }
                if (anchorIdx >= 0) {
                    settingsItems.add(anchorIdx + 1, positioned.item)
                } else {
                    settingsItems.add(positioned.item)
                }
            }

        state.extraItems.filter { it.position is InsertPosition.AtEnd }
            .forEach { settingsItems.add(it.item) }

        settingsItems
    }

    private data class SettingsState(
        val appBlockEnabled: Boolean,
        val fileCleanupEnabled: Boolean,
        val callBlockEnabled: Boolean,
        val hasPin: Boolean,
        val extraItems: List<PositionedItem>,
        val refreshTrigger: Int
    )

    // ── Public API ────────────────────────────────────────────────────────────

    fun addItemAt(index: Int, item: SettingItem) {
        updateExtraItems(item, InsertPosition.AtIndex(index))
    }

    fun addItemAfter(anchorId: Int, item: SettingItem) {
        updateExtraItems(item, InsertPosition.AfterItem(anchorId))
    }

    fun addItemAtEnd(item: SettingItem) {
        updateExtraItems(item, InsertPosition.AtEnd)
    }

    fun removeExtraItem(itemId: Int) {
        _extraItems.value = _extraItems.value.filterNot { it.item.id == itemId }
        _refreshTrigger.value++
    }

    fun loadSettings() {
        _refreshTrigger.value++
    }

    fun updateItem(item: SettingItem) {
        _refreshTrigger.value++
    }

    private fun updateExtraItems(item: SettingItem, position: InsertPosition) {
        _extraItems.value = _extraItems.value
            .filterNot { it.item.id == item.id }
            .plus(PositionedItem(position, item))
        _refreshTrigger.value++
    }
}

class SettingsViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel() as T
    }
}
