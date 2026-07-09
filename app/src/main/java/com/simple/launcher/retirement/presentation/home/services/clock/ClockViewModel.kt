package com.simple.launcher.retirement.presentation.home.services.clock

import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.dp
import kotlinx.coroutines.flow.StateFlow

class ClockViewModel : BaseViewModel() {

    val timeViewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        initialValue = null
    ) { resources ->

        val list = listOf(buildClockItem(resources))

        value = GroupViewItem(0, list)
    }

    private fun buildClockItem(resources: Map<String, Any>): ClockHomeItem {

        return ClockHomeItem(screenWidth = calculateScreenWidth()).apply {

            buildDrawSpec(resources)
        }
    }

    private fun calculateScreenWidth(): Int {

        return android.content.res.Resources.getSystem().displayMetrics.widthPixels - 2 * 12.dp().toInt()
    }
}
