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
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.emptyText
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    /** Số file lạ hiện có trên máy — cập nhật real-time qua Flow */
    val strangeFileCount: StateFlow<Int> = FileRepository.instance.countStrangeFilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    private val _screenState = MutableStateFlow(CleanScreenState.IDLE)
    val screenState: StateFlow<CleanScreenState> = _screenState

    val toolbar: StateFlow<ToolbarState> = combineState(flow1 = strings, flow2 = themes, initialValue = ToolbarState.empty()) { stringMap, themeMap ->

        val color = themeMap.getColor(android.R.attr.textColorPrimary)

        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(R.string.clean_files_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val action: StateFlow<ActionState> = combineState(flow1 = strings, flow2 = themes, flow3 = screenState, initialValue = ActionState.empty()) { stringMap, themeMap, screenState ->

        val text = when (screenState) {
            CleanScreenState.IDLE -> R.string.clean_files_start
            CleanScreenState.SCANNING -> R.string.clean_files_running
            CleanScreenState.DONE -> R.string.clean_files_retry
        }.let {

            stringMap.getString(it)
        }

        val textColor = when (screenState) {
            CleanScreenState.SCANNING -> android.R.attr.textColorPrimary
            else -> com.google.android.material.R.attr.colorOnPrimary
        }.let {
            themeMap.getColor(it, Color.LTGRAY)
        }

        val bgColor = when (screenState) {
            CleanScreenState.SCANNING -> android.R.attr.colorControlHighlight
            else -> android.R.attr.colorPrimary
        }.let {
            themeMap.getColor(it, Color.LTGRAY)
        }

        buildActionState(
            text = text,
            textColor = textColor,
            backgroundColor = bgColor
        )
    }

    /**
     * Status text hiển thị dưới ring — màu và nội dung đều từ ViewModel.
     * Fragment chỉ observe và gọi binding.tvStatus.setText(it).
     */
    val statusText: StateFlow<RichText> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = _screenState,
        flow4 = strangeFileCount,
        initialValue = emptyText()
    ) { stringMap, themeMap, state, count ->
        val color = themeMap.getColor(android.R.attr.textColorSecondary)
        val text = when (state) {
            CleanScreenState.IDLE -> if (count > 0)
                String.format(stringMap.getString(R.string.clean_files_idle_desc), count)
            else
                stringMap.getString(R.string.clean_files_desc)
            CleanScreenState.SCANNING -> stringMap.getString(R.string.clean_files_running)
            CleanScreenState.DONE    -> stringMap.getString(R.string.clean_files_completed)
        }
        RichText.Builder(text)
            .with(ForegroundColor(color))
            .build()
    }

    private val _result = MutableStateFlow<CleanResultData?>(null)
    val result: StateFlow<CleanResultData?> = _result

    /**
     * Trạng thái vùng trung tâm ring — text, màu, icon visibility đều từ ViewModel.
     * Dùng 5 flows tối đa của combineState.
     */
    val ringCenter: StateFlow<RingCenterState> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = _screenState,
        flow4 = strangeFileCount,
        flow5 = _result,
        initialValue = RingCenterState(showIcon = true)
    ) { stringMap, themeMap, state, count, result ->
        val primaryColor   = themeMap.getColor(android.R.attr.textColorPrimary)
        val secondaryColor = themeMap.getColor(android.R.attr.textColorSecondary)
        when (state) {
            CleanScreenState.IDLE -> if (count > 0) RingCenterState(
                showIcon  = false,
                countText = RichText.Builder(count.toString())
                    .with(ForegroundColor(primaryColor))
                    .build(),
                unitText  = RichText.Builder(stringMap.getString(R.string.clean_files_count_unit))
                    .with(ForegroundColor(secondaryColor))
                    .build()
            ) else RingCenterState(showIcon = true)
            CleanScreenState.SCANNING -> RingCenterState(showIcon = true)
            CleanScreenState.DONE -> RingCenterState(
                showIcon  = false,
                countText = RichText.Builder((result?.totalFiles ?: 0).toString())
                    .with(ForegroundColor(primaryColor))
                    .build(),
                unitText  = RichText.Builder(stringMap.getString(R.string.clean_result_files_deleted))
                    .with(ForegroundColor(secondaryColor))
                    .build()
            )
        }
    }

    /**
     * List có size = StrangeFileCategory.values().size.
     * null  = chưa xử lý
     * non-null = Pair(số file đã xóa, bytes đã xóa)
     */
    private val _categoryResults = MutableStateFlow<List<Pair<Int, Long>?>>(emptyList())

    /**
     * Text đã được format + tô màu cho từng category count.
     * null = category chưa xử lý xong (Fragment giữ View INVISIBLE).
     */
    val categoryCountTexts: StateFlow<List<RichText?>> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = _categoryResults,
        initialValue = emptyList()
    ) { stringMap, themeMap, results ->
        val color = themeMap.getColor(android.R.attr.textColorSecondary)
        results.map { pair ->
            pair?.let {
                String.format(stringMap.getString(R.string.clean_cat_file_count), it.first)
                    .let { text -> RichText.Builder(text).with(ForegroundColor(color)).build() }
            }
        }
    }

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
    }

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

    /**
     * Trạng thái vùng trung tâm ring.
     * [showIcon] = true  → hiện icon, ẩn số đếm.
     * [showIcon] = false → ẩn icon, hiện [countText] + [unitText].
     */
    data class RingCenterState(
        val showIcon: Boolean,
        val countText: RichText = emptyText(),
        val unitText: RichText = emptyText()
    )
}
