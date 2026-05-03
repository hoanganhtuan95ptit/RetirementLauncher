package com.simple.launcher.retirement.presentation.app_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.usecase.GetSelectableAppsUseCase
import com.simple.launcher.retirement.domain.usecase.SaveSelectedAppsUseCase

class AppListViewModel(
    private val getSelectableAppsUseCase: GetSelectableAppsUseCase,
    private val saveSelectedAppsUseCase: SaveSelectedAppsUseCase
) : ViewModel() {

    private val _apps = MutableLiveData<List<SelectableAppEntity>>()
    val apps: LiveData<List<SelectableAppEntity>> = _apps

    fun loadApps() {
        _apps.value = getSelectableAppsUseCase()
    }

    fun saveSelection() {
        val selectedPackages = _apps.value?.filter { it.isSelected }?.map { it.app.packageName }?.toSet()
        if (selectedPackages != null) {
            saveSelectedAppsUseCase(selectedPackages)
        }
    }
}
