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
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.*
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.text.RichText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppListViewModel(
    private val getSelectableAppsUseCase: GetSelectableAppsUseCase,
    private val saveSelectedAppsUseCase: SaveSelectedAppsUseCase
) : BaseViewModel() {

    // Toolbar state — title với màu, size, font từ theme; backIcon với màu từ theme
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

    // Nội bộ: domain entities (không chứa trạng thái selected)
    private val _apps = MutableStateFlow<List<SelectableAppEntity>>(emptyList())
    private val _query = MutableStateFlow("")

    // Tách riêng trạng thái selected — độc lập với search query
    // Khởi tạo từ danh sách đã lưu trong cache
    private val _selectedIds = MutableStateFlow<Set<String>>(
        AppRepository.instance.getSelectedPackages().toSet()
    )

    // Expose ra ngoài: ViewItems đã được xử lý sẵn, adapter chỉ set data
    val items: StateFlow<List<SelectableAppItem>> = combineState(
        flow1 = _apps,
        flow2 = _query,
        flow3 = _selectedIds,
        initialValue = emptyList()
    ) { apps, query, selectedIds ->
        value = apps.filter {
            query.isBlank() || it.app.label.contains(query, ignoreCase = true)
        }.map { entity ->
            SelectableAppItem(
                label = RichText(entity.app.label),
                icon = ImageDrawable(entity.app.icon),
                isSelected = entity.app.packageName in selectedIds,
                entity = entity
            )
        }
    }

    fun search(text: String) {
        _query.value = text
    }

    fun loadApps() {
        _apps.value = getSelectableAppsUseCase()
    }

    // Nhận entity từ EventBus (adapter gửi nguyên entity, không toggle), ViewModel xử lý toggle
    fun updateItem(entity: SelectableAppEntity) {
        val packageName = entity.app.packageName
        val current = _selectedIds.value
        _selectedIds.value = if (packageName in current) {
            current - packageName
        } else {
            current + packageName
        }
    }

    /** Trả về tất cả package name đang được chọn (không phụ thuộc search query) */
    fun getAllSelectedIds(): Set<String> = _selectedIds.value

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
