package com.simple.launcher.retirement.presentation.clean_memory

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.theme.getColor
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.emptyText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CleanMemoryViewModel : BaseViewModel() {

    val toolbar: StateFlow<ToolbarState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val title = buildToolbarTitle(stringMap.getString(R.string.clean_memory_title), color)
        ToolbarState(title = title, backIcon = buildBackIcon(color))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ToolbarState.empty())

    private val _actionState = MutableStateFlow(R.string.clean_memory_start)

    val action: StateFlow<ActionState> = combine(strings, themes, _actionState) { stringMap, themeMap, actionRes ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        
        val text = stringMap.getString(actionRes)
            .toRich()
            .with(ForegroundColor(color), TextSize(18), Bold)
            
        val background = Background(
            backgroundColor = android.graphics.Color.LTGRAY,
            cornerRadius_TL = 12,
            cornerRadius_TR = 12,
            cornerRadius_BL = 12,
            cornerRadius_BR = 12
        )
            
        ActionState(text = text, background = background)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())

    fun setActionState(resId: Int) {
        _actionState.value = resId
    }
}
