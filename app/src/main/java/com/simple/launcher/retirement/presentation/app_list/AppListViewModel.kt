package com.simple.launcher.retirement.presentation.app_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.usecase.GetSelectableAppsUseCase
import com.simple.launcher.retirement.domain.usecase.SaveSelectedAppsUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.text.toRich

class AppListViewModel(
    private val getSelectableAppsUseCase: GetSelectableAppsUseCase,
    private val saveSelectedAppsUseCase: SaveSelectedAppsUseCase
) : BaseViewModel() {

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
