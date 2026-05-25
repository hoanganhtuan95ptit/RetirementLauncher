package com.simple.launcher.retirement.presentation.permissions.call_block

import android.graphics.Color
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.StateFlow

class CallBlockPermissionViewModel : BaseViewModel() {

    val title: StateFlow<RichText> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = RichText("")
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        RichText.Builder(stringMap.getString(R.string.call_block_title))
            .with(ForegroundColor(color), Bold)
            .build()
    }

    val description: StateFlow<RichText> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = RichText("")
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorSecondary, Color.GRAY)
        val highlightColor = themeMap.getColor(android.R.attr.colorAccent)

        RichText.Builder(stringMap.getString(R.string.call_block_desc))
            .with(ForegroundColor(color))
            .withFirst(stringMap.getString(R.string.call_block_highlight), Bold, ForegroundColor(highlightColor))
            .build()
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight, Color.LTGRAY)

        buildActionState(
            text = stringMap.getString(R.string.permission_grant),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
