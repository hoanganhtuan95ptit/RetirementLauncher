package com.simple.launcher.retirement.presentation.permissions.usage_stats

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.withFirst
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.StateFlow

class UsageStatsPermissionViewModel : BaseViewModel() {

    val title: StateFlow<RichText> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = RichText("")
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        stringMap.getString(R.string.usage_stats_permission_title)
            .withFirst(stringMap.getString(R.string.usage_stats_permission_title), ForegroundColor(color), Bold)
    }

    val description: StateFlow<RichText> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = RichText("")
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorSecondary)
        val highlightColor = themeMap.getColor(android.R.attr.colorAccent)

        stringMap.getString(R.string.usage_stats_permission_desc)
            .withFirst(stringMap.getString(R.string.usage_stats_permission_desc), ForegroundColor(color))
            .withFirst(stringMap.getString(R.string.usage_stats_permission_highlight), Bold, ForegroundColor(highlightColor))
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight, android.graphics.Color.LTGRAY)

        buildActionState(
            text = stringMap.getString(R.string.permission_grant),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
