package com.simple.launcher.retirement.presentation.app_list

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.usecase.GetSelectableAppsUseCase
import com.simple.launcher.retirement.domain.usecase.SaveSelectedAppsUseCase
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.SearchState
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildSearchState
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.VietnameseStringUtils
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSurface
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.toBig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect

/**
 * No-arg constructor để dùng được với `by viewModels()` mặc định (không cần Factory).
 * Dependencies là các singleton nội bộ nên đọc thẳng từ `.instance` — tránh phải khai báo
 * & truyền qua `ViewModelProvider.Factory`.
 */
class AppListViewModel : BaseViewModel() {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private val getSelectableAppsUseCase: GetSelectableAppsUseCase = GetSelectableAppsUseCase.instance
    private val saveSelectedAppsUseCase: SaveSelectedAppsUseCase = SaveSelectedAppsUseCase.instance

    // ── 2. Flows ──────────────────────────────────────────────────────────

    val query = MutableStateFlow("")

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->

        val color = resources.textColorPrimary
        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.setting_app_list), color),
            backIcon = buildBackIcon(color)
        )
    }

    val searchState: StateFlow<SearchState> = combineState(
        flow1 = resources,
        initialValue = SearchState.empty()
    ) { resources ->

        val textColor = resources.textColorPrimary
        val hintColor = resources.textColorSecondary
        val backgroundColor = resources.colorSurface

        value = buildSearchState(
            hint = resources.getString(R.string.search),
            textColor = textColor,
            hintColor = hintColor,
            backgroundColor = backgroundColor
        )
    }

    val saveAction: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary

        value = buildActionState(
            text = resources.getString(R.string.app_list_save_action),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    val apps: StateFlow<List<SelectableAppEntity>?> = mutableStateFlow(null) {

        getSelectableAppsUseCase.invoke().collect { value = it }
    }

    // Khởi tạo rỗng rồi nạp từ flow trong background. Sau lần load đầu, mọi thao tác
    // toggle của user tự cập nhật _selectedIds — không cần re-collect flow.
    private val _selectedIds: MutableStateFlow<Set<String>> = mutableStateFlow(emptySet()) {

        value = AppRepository.instance.getSelectedPackagesFlow().first().toSet()
    }

    private val preparedApps: StateFlow<List<PreparedAppEntry>?> = combineState(
        flow1 = apps.filterNotNull(),
        initialValue = null
    ) { apps ->

        value = apps.map { entity ->

            val rawLabel = entity.app.label
            PreparedAppEntry(
                entity = entity,
                lowerLabel = rawLabel.lowercase(),
                normalizedLabel = VietnameseStringUtils.normalizeForSearch(rawLabel),
                bigLabel = rawLabel.toBig(),
                bigIcon = BigImage(entity.app.icon)
            )
        }
    }

    val items: StateFlow<List<SelectableAppItem>> = combineState(
        flow1 = preparedApps.filterNotNull(),
        flow2 = query,
        flow3 = _selectedIds,
        initialValue = emptyList()
    ) { prepared, query, selectedIds ->

        val filtered = filterByQuery(prepared, query)
        value = filtered
            .sortedWith(compareBy({ it.second }, { it.first.lowerLabel }))
            .map { (entry, _) -> toSelectableAppItem(entry, selectedIds) }
    }

    // ── 3. Public API ─────────────────────────────────────────────────────

    fun search(text: String) {

        query.value = text
    }

    fun updateItem(entity: SelectableAppEntity) {

        val packageName = entity.app.packageName
        val current = _selectedIds.value
        _selectedIds.value = if (packageName in current) {

            current - packageName
        } else {

            current + packageName
        }
    }

    fun getAllSelectedIds(): Set<String> = _selectedIds.value

    /**
     * Xây danh sách package đã chọn theo đúng thứ tự cũ đã lưu:
     * - Giữ nguyên thứ tự các app cũ vẫn đang chọn
     * - App mới toggle thêm sẽ nằm cuối
     */
    suspend fun buildOrderedSelectedIds(): List<String> {

        val currentSelected = _selectedIds.value
        val savedIds = AppRepository.instance.getSelectedPackagesFlow().first()
        val ordered = savedIds.filter { it in currentSelected }.toMutableList()
        ordered.addAll(currentSelected.filter { it !in savedIds })
        return ordered
    }

    /**
     * Lưu list app đã chọn — TRỰC TIẾP dùng thứ tự cũ đã save + append app mới ở cuối.
     * Trước đây gọi `_selectedIds.value.toList()` — Set không đảm bảo order → Home
     * shuffle random sau mỗi lần save.
     */
    suspend fun saveSelection() {

        saveSelectedAppsUseCase(buildOrderedSelectedIds())
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun filterByQuery(
        apps: List<PreparedAppEntry>,
        query: String
    ): List<Pair<PreparedAppEntry, Int>> {

        if (query.isBlank()) return apps.map { it to 0 }

        // Normalize query 1 lần duy nhất (thay vì mỗi item x 3 lần).
        val normalizedQuery = VietnameseStringUtils.normalizeForSearch(query)
        return apps.mapNotNull { entry -> matchWithPriority(entry, normalizedQuery) }
    }

    private fun matchWithPriority(
        entry: PreparedAppEntry,
        normalizedQuery: String
    ): Pair<PreparedAppEntry, Int>? {

        val normalizedLabel = entry.normalizedLabel
        val priority = when {

            normalizedLabel == normalizedQuery -> 0
            normalizedLabel.startsWith(normalizedQuery) -> 1
            normalizedLabel.contains(normalizedQuery) -> 2
            else -> return null
        }
        return entry to priority
    }

    private fun toSelectableAppItem(
        entry: PreparedAppEntry,
        selectedIds: Set<String>
    ): SelectableAppItem = SelectableAppItem(
        label = entry.bigLabel,
        icon = entry.bigIcon,
        isSelected = entry.entity.app.packageName in selectedIds,
        entity = entry.entity
    )

    // ── 5. Nested classes ─────────────────────────────────────────────────

    /**
     * Wrapper cache toàn bộ giá trị precomputed / normalize cho 1 entry.
     * Chỉ tính khi `apps` thay đổi, KHÔNG tính lại mỗi keystroke như trước —
     * trước đó `combineState { apps, query, selectedIds -> map { toBig() } }`
     * tạo lại BigText/BigImage mỗi lần user gõ 1 ký tự → phá tác dụng của
     * "Precomputed UI Node Engine".
     */
    private data class PreparedAppEntry(
        val entity: SelectableAppEntity,
        val lowerLabel: String,
        val normalizedLabel: String,
        val bigLabel: BigText,
        val bigIcon: BigImage
    )
}
