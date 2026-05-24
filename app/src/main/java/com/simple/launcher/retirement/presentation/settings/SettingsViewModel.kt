package com.simple.launcher.retirement.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : BaseViewModel() {

    private val repository by lazy { PreferenceRepository.instance }

    // ── Toolbar ──────────────────────────────────────────────────────────────

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

    // ── Item map: key = thứ tự (Double), value = danh sách ViewItem tại slot đó ──
    // Base items được chèn thẳng vào map với ORDER_* cố định.
    // Services ghi đè / thêm slot của họ qua updateItem().
    private val _itemMap = MutableStateFlow<Map<Double, List<ViewItem>>>(emptyMap())

    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = repository.hasPinFlow(),
        flow4 = _itemMap,
        initialValue = emptyList()
    ) { stringMap, themeMap, hasPin, itemMap ->

        val textColor = themeMap.getColor(android.R.attr.textColorPrimary)

        fun Int.toSettingRichText() =
            stringMap.getString(this).toRich().with(ForegroundColor(textColor))

        // ── Build base slots ──────────────────────────────────────────────────
        val baseSlots = buildList<Pair<Double, List<ViewItem>>> {
            // Group 1: General
            add(SettingItem.ORDER_HEADER_GENERAL to listOf(
                SettingHeaderItem(R.string.setting_header_general.toSettingRichText())
            ))
            add(SettingItem.ORDER_DEFAULT_LAUNCHER to listOf(
                SettingItem(SettingItem.ID_DEFAULT_LAUNCHER, R.string.setting_default_launcher.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_manage))
            ))
            add(SettingItem.ORDER_APP_LIST to listOf(
                SettingItem(SettingItem.ID_APP_LIST, R.string.setting_app_list.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_agenda))
            ))
            add(SettingItem.ORDER_CONTACT_LIST to listOf(
                SettingItem(SettingItem.ID_CONTACT_LIST, R.string.setting_contact_list.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_call))
            ))

            // Group 2: Security & Protection
            add(SettingItem.ORDER_HEADER_SECURITY to listOf(
                SettingHeaderItem(R.string.setting_header_security.toSettingRichText())
            ))
            if (hasPin)add(SettingItem.ORDER_PIN to listOf(
                SettingItem(SettingItem.ID_PIN, R.string.setting_pin.toSettingRichText(), ImageRes(android.R.drawable.ic_lock_idle_lock))
            ))

            // Group 3: Optimization & Utilities
            add(SettingItem.ORDER_HEADER_OPTIMIZATION to listOf(
                SettingHeaderItem(R.string.setting_header_optimization.toSettingRichText())
            ))
            add(SettingItem.ORDER_CLEAN_FILES to listOf(
                SettingItem(SettingItem.ID_CLEAN_FILES, R.string.setting_clean_files.toSettingRichText(), ImageRes(android.R.drawable.ic_menu_delete))
            ))
            add(SettingItem.ORDER_CLEAN_MEMORY to listOf(
                SettingItem(SettingItem.ID_CLEAN_MEMORY, R.string.setting_clean_memory.toSettingRichText(), ImageRes(android.R.drawable.ic_media_play))
            ))

            // ── Debug group: chỉ hiển thị trong debug build ──────────────────
            if (BuildConfig.DEBUG) {
                add(SettingItem.ORDER_HEADER_DEBUG to listOf(
                    SettingHeaderItem(R.string.setting_header_debug.toSettingRichText())
                ))
                add(SettingItem.ORDER_DEBUG_BLOCK_SCREEN to listOf(
                    SettingItem(SettingItem.ID_DEBUG_BLOCK_SCREEN, R.string.setting_debug_block_screen.toSettingRichText(), ImageRes(android.R.drawable.ic_lock_idle_lock))
                ))
            }
        }

        // ── Merge base + service slots, sort by order key, flatten ────────────
        (baseSlots + itemMap.toList())
            .sortedBy { it.first }
            .flatMap { it.second }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Đặt (hoặc cập nhật) danh sách ViewItem tại slot [order].
     * Truyền list rỗng để xoá slot đó khỏi danh sách.
     *
     * Ví dụ:
     *   settingsViewModel.updateItem(SettingItem.ORDER_TOGGLE_BLOCK, listOf(toggleItem))
     */
    fun updateItem(order: Double, list: List<ViewItem>) {
        _itemMap.value = _itemMap.value.toMutableMap().apply {
            put(order, list)
        }
    }
}

class SettingsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel() as T
    }
}
