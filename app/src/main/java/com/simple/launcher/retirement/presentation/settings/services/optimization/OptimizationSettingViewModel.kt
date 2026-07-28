package com.simple.launcher.retirement.presentation.settings.services.optimization

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.settings.services.settingHeader
import com.simple.launcher.retirement.utils.exts.combineState
import kotlinx.coroutines.flow.StateFlow

class OptimizationSettingViewModel : BaseViewModel() {

    val viewItemList: StateFlow<GroupViewItem?> = combineState(resources, null) { resources ->

        value = buildOptimizationGroup(resources)
    }

    private fun buildOptimizationGroup(resources: Map<String, Any>): GroupViewItem {

        val items = buildList {

            add(
                settingHeader(
                    title = R.string.setting_header_optimization,
                    resources = resources
                )
            )

            /*
            add(
                settingItem(
                    id = SettingItem.ID_DEBUG_BLOCK_SCREEN,
                    icon = R.drawable.ic_bug_report_black_24dp,
                    title = R.string.setting_debug_block_screen,
                    resources = resources
                )
            )
            */
        }

        return GroupViewItem(order = 2, list = items)
    }
}
