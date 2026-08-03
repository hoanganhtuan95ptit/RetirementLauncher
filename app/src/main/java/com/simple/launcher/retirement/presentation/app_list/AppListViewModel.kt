package com.simple.launcher.retirement.presentation.app_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.simple.ui.precompute.text.toBig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class AppListViewModel(
    private val getSelectableAppsUseCase: GetSelectableAppsUseCase,
    private val saveSelectedAppsUseCase: SaveSelectedAppsUseCase
) : BaseViewModel() {

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

    val query = MutableStateFlow("")

    val apps: StateFlow<List<SelectableAppEntity>?> = mutableStateFlow(null) {

        value = getSelectableAppsUseCase.invoke()
    }

    // Khởi tạo rỗng rồi nạp từ flow trong background. Sau lần load đầu, mọi thao tác
    // toggle của user tự cập nhật _selectedIds — không cần re-collect flow.
    private val _selectedIds: MutableStateFlow<Set<String>> = mutableStateFlow(emptySet()) {

        value = AppRepository.instance.getSelectedPackagesFlow().first().toSet()
    }

    val items: StateFlow<List<SelectableAppItem>> = combineState(
        flow1 = apps.filterNotNull(),
        flow2 = query,
        flow3 = _selectedIds,
        initialValue = emptyList()
    ) { apps, query, selectedIds ->

        val filtered = filterByQuery(apps, query)
        value = filtered
            .sortedWith(compareBy({ it.second }, { it.first.app.label.lowercase() }))
            .map { (entity, _) -> toSelectableAppItem(entity, selectedIds) }
    }

    private fun filterByQuery(
        apps: List<SelectableAppEntity>,
        query: String
    ): List<Pair<SelectableAppEntity, Int>> {

        if (query.isBlank()) return apps.map { it to 0 }
        return apps.mapNotNull { entity -> matchWithPriority(entity, query) }
    }

    private fun matchWithPriority(
        entity: SelectableAppEntity,
        query: String
    ): Pair<SelectableAppEntity, Int>? {

        val label = entity.app.label
        val priority = when {

            VietnameseStringUtils.equalsIgnoreDiacritics(label, query) -> 0
            VietnameseStringUtils.startsWithIgnoreDiacritics(label, query) -> 1
            VietnameseStringUtils.containsIgnoreDiacritics(label, query) -> 2
            else -> return null
        }
        return entity to priority
    }

    private fun toSelectableAppItem(
        entity: SelectableAppEntity,
        selectedIds: Set<String>
    ): SelectableAppItem = SelectableAppItem(
        label = entity.app.label.toBig(),
        icon = BigImage(entity.app.icon),
        isSelected = entity.app.packageName in selectedIds,
        entity = entity
    )

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

    fun saveSelection() {

        saveSelectedAppsUseCase(_selectedIds.value.toList())
    }
}

class AppListViewModelFactory(
    private val getSelectableAppsUseCase: GetSelectableAppsUseCase,
    private val saveSelectedAppsUseCase: SaveSelectedAppsUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AppListViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return AppListViewModel(getSelectableAppsUseCase, saveSelectedAppsUseCase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
