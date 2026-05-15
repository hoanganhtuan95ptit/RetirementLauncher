package com.simple.launcher.retirement.presentation.app_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.usecase.GetSelectableAppsUseCase
import com.simple.launcher.retirement.domain.usecase.SaveSelectedAppsUseCase
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.theme.getColor
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
    private val _apps = MutableLiveData<List<SelectableAppEntity>>()

    // Expose ra ngoài: ViewItems đã được xử lý sẵn, adapter chỉ set data
    val items: LiveData<List<SelectableAppItem>> = _apps.map { entities ->
        entities.map { entity ->
            SelectableAppItem(
                label = entity.app.label.toRich(),
                icon = ImageDrawable(entity.app.icon),
                isSelected = entity.isSelected,
                entity = entity
            )
        }
    }

    fun loadApps() {
        _apps.value = getSelectableAppsUseCase()
    }

    // Nhận entity từ EventBus (adapter gửi nguyên entity, không toggle), ViewModel xử lý toggle
    fun updateItem(entity: SelectableAppEntity) {
        val currentList = _apps.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.app.packageName == entity.app.packageName }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isSelected = !currentList[index].isSelected)
            _apps.value = currentList
        }
    }

    fun saveSelection() {
        val selectedPackages = _apps.value?.filter { it.isSelected }?.map { it.app.packageName }?.toSet()
        if (selectedPackages != null) {
            saveSelectedAppsUseCase(selectedPackages)
        }
    }
}
