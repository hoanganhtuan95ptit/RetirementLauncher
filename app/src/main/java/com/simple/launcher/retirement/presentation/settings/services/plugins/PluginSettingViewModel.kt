package com.simple.launcher.retirement.presentation.settings.services.plugins

import androidx.lifecycle.viewModelScope
import com.simple.adapter.ViewItem
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ViewItemViewModel
import com.simple.launcher.retirement.presentation.settings.adapters.SettingHeaderItem
import com.simple.launcher.retirement.presentation.settings.services.settingHeader
import com.simple.launcher.retirement.utils.exts.combineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class PluginSettingViewModel : ViewItemViewModel() {

    val enable = MutableStateFlow<Boolean?>(null)

    val titleViewItem: StateFlow<List<ViewItem>> = combineState(
        resources,
        enable.filterNotNull(),
        emptyList()
    ) { resources, enable ->

        val list = arrayListOf<ViewItem>()

        if (enable) settingHeader(
            title = R.string.setting_header_plugins,
            resources = resources
        ).let {

            list.add(it)
        }

        value = list
    }

    init {

        viewItemList.map { items ->

            items.any { it !is SettingHeaderItem }
        }.launchCollect(viewModelScope) {

            enable.value = it
        }

        titleViewItem.launchCollect(viewModelScope) {

            updateItem(0, it)
        }
    }
}
