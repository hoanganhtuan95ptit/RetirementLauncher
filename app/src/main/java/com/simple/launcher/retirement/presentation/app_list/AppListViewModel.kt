package com.simple.launcher.retirement.presentation.app_list

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
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
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AppListViewModel(
    private val getSelectableAppsUseCase: GetSelectableAppsUseCase,
    private val saveSelectedAppsUseCase: SaveSelectedAppsUseCase
) : BaseViewModel() {

    // Toolbar state — title với màu, size, font từ theme; backIcon với màu từ theme
    val toolbar: StateFlow<ToolbarState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val title = buildToolbarTitle(stringMap.getString(R.string.setting_app_list), color)
        ToolbarState(title = title, backIcon = buildBackIcon(color))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ToolbarState.empty())

    val searchState: StateFlow<SearchState> = combine(strings, themes) { stringMap, themeMap ->
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val hintColor = themeMap.getColor(android.R.attr.textColorSecondary) ?: android.graphics.Color.GRAY
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: android.graphics.Color.LTGRAY

        buildSearchState(
            hint = stringMap.getString(R.string.search),
            textColor = textColor,
            hintColor = hintColor,
            backgroundColor = backgroundColor
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchState.empty())

    val saveAction: StateFlow<ActionState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: android.graphics.Color.LTGRAY

        buildActionState(
            text = stringMap.getString(R.string.app_list_save_action),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())

    // Nội bộ: domain entities
    private val _apps = MutableStateFlow<List<SelectableAppEntity>>(emptyList())
    private val _query = MutableStateFlow("")

    // Expose ra ngoài: ViewItems đã được xử lý sẵn, adapter chỉ set data
    val items: StateFlow<List<SelectableAppItem>> = combine(_apps, _query) { apps, query ->
        filterApps(apps, query)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun filterApps(entities: List<SelectableAppEntity>, query: String): List<SelectableAppItem> {
        return entities.filter {
            query.isBlank() || it.app.label.contains(query, ignoreCase = true)
        }.map { entity ->
            SelectableAppItem(
                label = entity.app.label.toRich(),
                icon = ImageDrawable(entity.app.icon),
                isSelected = entity.isSelected,
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
        val currentList = _apps.value.toMutableList()
        val index = currentList.indexOfFirst { it.app.packageName == entity.app.packageName }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isSelected = !currentList[index].isSelected)
            _apps.value = currentList
        }
    }

    fun saveSelection() {
        val selectedPackages = _apps.value.filter { it.isSelected }.map { it.app.packageName }.toSet()
        saveSelectedAppsUseCase(selectedPackages)
    }
}
