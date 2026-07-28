package com.simple.launcher.retirement.presentation.clock

import android.content.res.Resources
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.clock.adapters.ClockHomeItem
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import kotlinx.coroutines.flow.StateFlow

class ClockHomeViewModel : BaseViewModel() {

    private val repo = PreferenceRepository.instance

    val timeViewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        flow2 = repo.is24HourFormatFlow(),
        flow3 = repo.isAmPmEnabledFlow(),
        flow4 = repo.isSolarCalendarEnabledFlow(),
        flow5 = repo.lunarCalendarEnabledFlow(),
        initialValue = null
    ) { resources, is24h, isAmPm, isSolar, isLunar ->

        val list = listOf(
            buildClockItem(
                resources = resources,
                is24h = is24h,
                isAmPm = isAmPm,
                isSolar = isSolar,
                isLunar = isLunar
            )
        )

        value = GroupViewItem(0, list)
    }

    private fun buildClockItem(
        resources: Map<String, Any>,
        is24h: Boolean,
        isAmPm: Boolean,
        isSolar: Boolean,
        isLunar: Boolean
    ): ClockHomeItem {

        return ClockHomeItem(
            screenWidth = calculateScreenWidth(),
            is24h = is24h,
            isAmPm = isAmPm,
            isSolar = isSolar,
            isLunar = isLunar,
            solarPattern = resources.getString(R.string.solar_date_format),
            lunarPattern = resources.getString(R.string.lunar_date_format)
        ).apply {

            buildDrawSpec(resources)
        }
    }

    private fun calculateScreenWidth(): Int {

        return Resources.getSystem().displayMetrics.widthPixels - 2 * 12.dp().toInt()
    }
}
