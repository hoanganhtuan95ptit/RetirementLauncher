package com.simple.launcher.retirement.presentation.clean_memory

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.theme.getColor
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
}
