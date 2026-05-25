package com.simple.launcher.retirement.presentation.clean_files

import android.graphics.Color
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.StrangeFileCategory
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CleanScreenState { IDLE, SCANNING, DONE }

data class CategoryMeta(
    val labelRes: Int,
    val iconRes: Int,
    val iconBgRes: Int,
    val iconTintRes: Int
)

data class CleanResultData(
    val totalFiles: Int,
    val totalBytes: Long
) {
    val spaceMB: Float get() = totalBytes / (1024f * 1024f)
    val spaceLabel: String get() = if (spaceMB >= 1f) "%.1f MB".format(spaceMB) else "${totalBytes / 1024} KB"
}

class CleanFilesViewModel : BaseViewModel() {

    val categoryMeta: List<CategoryMeta> = listOf(
        CategoryMeta(
            R.string.clean_cat_system_temp,
            R.drawable.ic_home_drives_24dp,
            R.drawable.bg_category_icon_purple,
            R.color.clean_cat_purple
        ),
        CategoryMeta(
            R.string.clean_cat_compressed,
            R.drawable.ic_home_drives_24dp,
            R.drawable.bg_category_icon_amber,
            R.color.clean_cat_amber
        ),
        CategoryMeta(
            R.string.clean_cat_no_extension,
            R.drawable.ic_home_family_24dp,
            R.drawable.bg_category_icon_red,
            R.color.clean_cat_red
        ),
        CategoryMeta(
            R.string.clean_cat_apk_cache,
            R.drawable.ic_home_boost_24dp,
            R.drawable.bg_category_icon_green,
            R.color.clean_cat_green
        )
    )

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ToolbarState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
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
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary)
        val bgColor = themeMap.getColor(android.R.attr.colorControlHighlight, Color.LTGRAY)
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

    /**
     * List có size = StrangeFileCategory.values().size.
     * null  = chưa xử lý
     * non-null = Pair(số file đã xóa, bytes đã xóa)
     */
    private val _categoryResults = MutableStateFlow<List<Pair<Int, Long>?>>(emptyList())
    val categoryResults: StateFlow<List<Pair<Int, Long>?>> = _categoryResults

    // ─── Scan logic ────────────────────────────────────────────────────────────

    fun startScan() {
        if (_screenState.value == CleanScreenState.SCANNING) return

        viewModelScope.launch {
            val categories = StrangeFileCategory.values()
            val results = MutableList<Pair<Int, Long>?>(categories.size) { null }

            _result.value = null
            _categoryResults.value = results.toList()
            setScreenState(CleanScreenState.SCANNING)

            var totalFiles = 0
            var totalBytes = 0L

            categories.forEachIndexed { index, category ->
                val pair = withContext(Dispatchers.IO) {
                    FileRepository.instance.deleteStrangeFilesByCategory(category)
                }
                totalFiles += pair.first
                totalBytes += pair.second
                results[index] = pair
                _categoryResults.value = results.toList()
                delay(350)
            }

            _result.value = CleanResultData(totalFiles, totalBytes)
            FileRepository.instance.refreshFileStatus()
            setScreenState(CleanScreenState.DONE)
        }
    }

    // ─── State helpers ──────────────────────────────────────────────────────────

    fun setScreenState(state: CleanScreenState) {
        _screenState.value = state
        when (state) {
            CleanScreenState.IDLE     -> _actionRes.value = R.string.clean_files_start
            CleanScreenState.SCANNING -> _actionRes.value = R.string.clean_files_running
            CleanScreenState.DONE     -> _actionRes.value = R.string.clean_files_retry
        }
    }

    fun reset() {
        _screenState.value = CleanScreenState.IDLE
        _result.value = null
        _actionRes.value = R.string.clean_files_start
        _categoryResults.value = emptyList()
    }
}
