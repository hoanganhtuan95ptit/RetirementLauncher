package com.simple.launcher.retirement.presentation.clean_files

import android.graphics.Color
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

enum class CleanScreenState { IDLE, SCANNING, DONE }

data class CleanResultData(
    val totalFiles: Int,
    val totalBytes: Long
) {
    val spaceMB: Float get() = totalBytes / (1024f * 1024f)
    val spaceLabel: String get() = if (spaceMB >= 1f) "%.1f MB".format(spaceMB) else "${totalBytes / 1024} KB"
}

class CleanFilesViewModel : BaseViewModel() {

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ToolbarState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: Color.BLACK
        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(R.string.clean_files_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    private val _actionRes = MutableStateFlow(R.string.clean_files_start)

    val action: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = _actionRes,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap, actionRes ->
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary) ?: Color.BLACK
        val bgColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: Color.LTGRAY
        buildActionState(
            text = stringMap.getString(actionRes),
            textColor = textColor,
            backgroundColor = bgColor
        )
    }

    private val _screenState = MutableStateFlow(CleanScreenState.IDLE)
    val screenState: StateFlow<CleanScreenState> = _screenState

    private val _result = MutableStateFlow<CleanResultData?>(null)
    val result: StateFlow<CleanResultData?> = _result

    fun setScreenState(state: CleanScreenState) {
        _screenState.value = state
        when (state) {
            CleanScreenState.IDLE    -> _actionRes.value = R.string.clean_files_start
            CleanScreenState.SCANNING -> _actionRes.value = R.string.clean_files_running
            CleanScreenState.DONE    -> _actionRes.value = R.string.clean_files_retry
        }
    }

    fun setResult(totalFiles: Int, totalBytes: Long) {
        _result.value = CleanResultData(totalFiles, totalBytes)
    }

    fun reset() {
        _screenState.value = CleanScreenState.IDLE
        _result.value = null
        _actionRes.value = R.string.clean_files_start
    }
}
