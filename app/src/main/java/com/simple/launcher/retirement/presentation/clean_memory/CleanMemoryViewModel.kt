package com.simple.launcher.retirement.presentation.clean_memory

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CleanMemoryViewModel : BaseViewModel() {

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ToolbarState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(R.string.clean_memory_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    private val _actionState = MutableStateFlow(R.string.clean_memory_start)

    val action: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = _actionState,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap, actionRes ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: android.graphics.Color.LTGRAY

        buildActionState(
            text = stringMap.getString(actionRes),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    fun setActionState(resId: Int) {
        _actionState.value = resId
    }
}
