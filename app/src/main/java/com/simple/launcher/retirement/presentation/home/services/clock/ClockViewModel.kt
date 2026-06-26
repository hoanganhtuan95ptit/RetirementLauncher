package com.simple.launcher.retirement.presentation.home.services.clock

import android.graphics.Color
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.size.DP
import kotlinx.coroutines.flow.StateFlow

class ClockViewModel : BaseViewModel() {

    val timeViewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        null
    ) {

        val list = ClockHomeItem(
            background = Background.Builder()
                .backgroundColor(Color.WHITE)
                .cornerRadius(DP.DP_24)
                .build()
        ).let {

            listOf(it)
        }

        GroupViewItem(0, list)
    }
}