package com.simple.launcher.retirement.presentation.settings.services.optimization

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.settingHeader
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.StateFlow

class OptimizationSettingViewModel : BaseViewModel() {

    val viewItemList: StateFlow<GroupViewItem?> = combineState(resources, null) { resources ->

        buildOptimizationGroup(resources)
    }

    private fun buildOptimizationGroup(resources: Map<String, Any>): GroupViewItem {

        val items = buildList {

            add(
                settingHeader(
                    title = R.string.setting_header_optimization,
                    resources = resources
                )
            )

            add(
                settingItem(
                    id = SettingItem.ID_CLEAN_FILES,
                    icon = android.R.drawable.ic_menu_delete,
                    title = R.string.setting_clean_files,
                    resources = resources
                )
            )

            add(
                settingItem(
                    id = SettingItem.ID_CLEAN_MEMORY,
                    icon = android.R.drawable.ic_media_play,
                    title = R.string.setting_clean_memory,
                    resources = resources
                )
            )
        }

        return GroupViewItem(order = 2, list = items)
    }
}
