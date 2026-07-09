package com.simple.launcher.retirement.presentation.clean_files

import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.StrangeFileCategory
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorOnSurface
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSurface
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.orZero
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.exts.withAlpha
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import com.simple.launcher.retirement.utils.text.withStyleBodyMedium
import com.simple.launcher.retirement.utils.text.withStyleHeadlineSmall
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.emptyImage
import com.simple.ui.precompute.image.toBigImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.emptyText
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.with
import com.simple.ui.precompute.text.withFirst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanFilesViewModel : BaseViewModel() {

    /** Số file lạ hiện có trên máy — cập nhật real-time qua Flow */
    val strangeFileCount: StateFlow<Int> = FileRepository.instance.countStrangeFilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    val screenState = MutableStateFlow<ClearState>(ClearState.IDLE)

    val toolbar: StateFlow<ToolbarState> = combineState(flow1 = resources, initialValue = ToolbarState.empty()) { resources ->

        val color = resources.textColorPrimary

        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.clean_files_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val action: StateFlow<ActionState> = combineState(flow1 = resources, flow2 = screenState, initialValue = ActionState.empty()) { resourceMap, screenState ->

        val labels = when {
            screenState is ClearState.IDLE -> resourceMap.getString(R.string.clean_files_start)
            screenState is ClearState.Scanning -> resourceMap.getString(R.string.clean_files_running)
            else -> resourceMap.getString(R.string.clean_files_retry)
        }

        val backgroundColor = when {
            screenState is ClearState.IDLE -> resourceMap.colorPrimary
            screenState is ClearState.Scanning -> resourceMap.colorPrimary.withAlpha(0.2f)
            else -> resourceMap.colorPrimary
        }

        val textColor = resourceMap.colorOnPrimary

        value = ActionState(
            text = labels
                .withStyleHeadlineSmall()
                .with(BigForegroundColor(textColor), BigBold)
                .build(),

            image = R.drawable.ic_clear_files_black_24dp.toBigImage(resourceMap.colorOnPrimary),
            imageShow = true,

            background = Background.Builder()
                .backgroundColor(backgroundColor)
                .cornerRadius(DP.DP_24)
                .build()
        )
    }

    val ringViewData: StateFlow<RingViewData> = combineState(
        flow1 = resources,
        flow2 = screenState,
        flow3 = strangeFileCount,
        initialValue = RingViewData(showIcon = true)
    ) { resources, state, count ->

        val primaryColor = resources.textColorPrimary
        val secondaryColor = resources.textColorSecondary

        val resultStr by lazy {
            state.asObjectOrNull<ClearState.Done>()?.totalFiles.orZero().toString()
        }

        value = if (state is ClearState.IDLE && count > 0) RingViewData(
            text = (count.toString() + "\n" + resources.getString(R.string.clean_files_count_unit))
                .withStyleBodyLarge()
                .with(BigForegroundColor(secondaryColor))
                .withFirst(count.toString(), BigForegroundColor(primaryColor), BigTextSize(26), BigBold)
                .build()
        ) else if (state is ClearState.IDLE || state is ClearState.Scanning) RingViewData(
            showIcon = true,
            icon = R.drawable.ic_clear_files_black_24dp.toBigImage(resources.colorPrimary),
        ) else RingViewData(
            text = (resultStr + "\n" + resources.getString(R.string.clean_result_files_deleted))
                .withStyleBodyLarge()
                .with(BigForegroundColor(secondaryColor))
                .withFirst(resultStr, BigForegroundColor(primaryColor), BigTextSize(26), BigBold)
                .build(),
        )
    }

    val statusText: StateFlow<BigText> = combineState(flow1 = resources, flow2 = screenState, flow3 = strangeFileCount, initialValue = emptyText()) { resources, state, count ->

        val color = resources.textColorSecondary
        val text = buildStatusText(resources = resources, state = state, count = count)

        value = text
            .with(BigForegroundColor(color))
            .build()
    }

    val categoryViewDataList: StateFlow<List<CategoryViewData>> = combineState(flow1 = resources, flow2 = screenState, initialValue = emptyList()) { resources, state ->

        value = CATEGORY_META.map {

            val numberFile = buildCategoryFileCount(resources = resources, state = state, category = it.id)
            val showSelected = state is ClearState.Run && state.categoryMap[it.id] != null

            CategoryViewData(
                image = it.iconRes.toBigImage(it.color),
                imageBackground = Background.Builder()
                    .backgroundColor(it.color.withAlpha(0.2f))
                    .cornerRadius(DP.DP_8)
                    .build(),
                label = resources.getString(it.labelRes)
                    .withStyleBodyLarge()
                    .with(BigForegroundColor(resources.colorOnSurface))
                    .build(),
                numberFile = numberFile
                    .withStyleBodyMedium()
                    .with(BigForegroundColor(resources.colorOnSurface))
                    .build(),
                showSelected = showSelected
            )
        }
    }

    private fun buildStatusText(resources: Map<String, Any>, state: ClearState, count: Int): String {

        if (state is ClearState.IDLE) {

            if (count > 0) {

                return resources.getString(R.string.clean_files_idle_desc).replace("\$number_file", "$count")
            }

            return resources.getString(R.string.clean_files_desc)
        }

        if (state is ClearState.Scanning) {

            return resources.getString(R.string.clean_files_running)
        }

        return resources.getString(R.string.clean_files_completed)
    }

    private fun buildCategoryFileCount(
        resources: Map<String, Any>,
        state: ClearState,
        category: StrangeFileCategory
    ): String {

        if (state !is ClearState.Run) {

            return ""
        }

        val count = state.categoryMap[category] ?: return ""
        return resources.getString(R.string.clean_cat_file_count).replace("\$number_file", "$count")
    }

    val resultViewData: StateFlow<ResultViewData> = combineState(
        flow1 = resources,
        flow2 = screenState,
        initialValue = ResultViewData(show = false)
    ) { res, state ->

        if (state !is ClearState.Done) {
            value = ResultViewData(show = false)
        } else {
            val spaceMB = state.totalBytes / (1024f * 1024f)
            val spaceLabel = if (spaceMB >= 1f) "%.1f MB".format(spaceMB) else "${state.totalBytes / 1024} KB"

            value = ResultViewData(
                show = true,
                title = res.getString(R.string.clean_result_title)
                    .withStyleBodyLarge()
                    .with(BigForegroundColor(res.colorOnSurface))
                    .build(),

                resultFilesImage = R.drawable.ic_file_black_24dp.toBigImage("#FFBB00".toColorInt()),
                resultFilesLabel = "${state.totalFiles.orZero()}\n${res.getString(R.string.clean_result_files_deleted)}"
                    .withStyleBodyMedium()
                    .with(BigForegroundColor(res.colorOnSurface))
                    .withFirst("${state.totalFiles.orZero()}", BigTextSize(24), BigBold)
                    .build(),
                resultFilesBackground = Background.Builder()
                    .backgroundColor(res.colorSurface)
                    .cornerRadius(DP.DP_24)
                    .build(),

                resultSpaceImage = R.drawable.ic_clear_files_black_24dp.toBigImage("#FF4343".toColorInt()),
                resultSpaceLabel = "${spaceLabel}\n${res.getString(R.string.clean_result_space_freed)}"
                    .withStyleBodyMedium()
                    .with(BigForegroundColor(res.colorOnSurface))
                    .withFirst(spaceLabel, BigTextSize(24), BigBold)
                    .build(),
                resultSpaceBackground = Background.Builder()
                    .backgroundColor(res.colorSurface)
                    .cornerRadius(DP.DP_24)
                    .build()
            )
        }
    }

    val screenViewData: StateFlow<ScreenViewData> = combineState(
        flow1 = ringViewData,
        flow2 = statusText,
        flow3 = categoryViewDataList,
        flow4 = resultViewData,
        initialValue = ScreenViewData()
    ) { ringCenter, statusText, categoryViewDataList, resultViewData ->

        value = ScreenViewData(
            ringViewData = ringCenter,
            status = statusText,
            categoryViewDataList = categoryViewDataList,
            resultViewData = resultViewData
        )
    }

    fun startScan() = viewModelScope.launch {

        if (screenState.value is ClearState.Scanning) return@launch

        val categoryMap = hashMapOf<StrangeFileCategory, Int>()

        var totalFiles = 0
        var totalBytes = 0L

        StrangeFileCategory.entries.toTypedArray().forEachIndexed { _, category ->

            val pair = withContext(Dispatchers.IO) {
                FileRepository.instance.deleteStrangeFilesByCategory(category)
            }

            totalFiles += pair.first
            totalBytes += pair.second

            categoryMap[category] = pair.first
            screenState.value = ClearState.Scanning(categoryMap.toMap())

            delay(350)
        }

        FileRepository.instance.refreshFileStatus()

        screenState.value = ClearState.Done(categoryMap, totalFiles, totalBytes)
    }

    sealed class ClearState {

        object IDLE : ClearState()

        sealed class Run(
            open val categoryMap: Map<StrangeFileCategory, Int>
        ) : ClearState()

        data class Scanning(
            override val categoryMap: Map<StrangeFileCategory, Int>
        ) : Run(categoryMap)

        data class Done(
            override val categoryMap: Map<StrangeFileCategory, Int>,
            val totalFiles: Int,
            val totalBytes: Long
        ) : Run(categoryMap)
    }

    data class CategoryMeta(
        val id: StrangeFileCategory,
        val labelRes: Int,
        val iconRes: Int,
        val color: Int,
    )

    data class RingViewData(
        val icon: BigImage = emptyImage(),
        val text: BigText = emptyText(),
        val showIcon: Boolean = false,
    )

    data class CategoryViewData(
        val image: BigImage = emptyImage(),
        val imageBackground: Background = Background(),

        val label: BigText = emptyText(),
        val numberFile: BigText = emptyText(),

        val showSelected: Boolean = false
    )

    data class ResultViewData(
        val show: Boolean = false,

        val title: BigText = emptyText(),

        val resultFilesLabel: BigText = emptyText(),
        val resultFilesImage: BigImage = emptyImage(),
        val resultFilesBackground: Background = Background(),

        val resultSpaceLabel: BigText = emptyText(),
        val resultSpaceImage: BigImage = emptyImage(),
        val resultSpaceBackground: Background = Background(),
    )

    data class ScreenViewData(
        val ringViewData: RingViewData = RingViewData(),
        val status: BigText = emptyText(),
        val categoryViewDataList: List<CategoryViewData> = emptyList(),
        val resultViewData: ResultViewData = ResultViewData(),
    )

    companion object {

        private val CATEGORY_META = listOf(
            CategoryMeta(
                id = StrangeFileCategory.SYSTEM_TEMP,
                labelRes = R.string.clean_cat_system_temp,
                iconRes = R.drawable.ic_home_drives_24dp,
                color = "#192850".toColorInt()
            ),
            CategoryMeta(
                id = StrangeFileCategory.COMPRESSED,
                labelRes = R.string.clean_cat_compressed,
                iconRes = R.drawable.ic_home_drives_24dp,
                color = "#4196FF".toColorInt()
            ),
            CategoryMeta(
                id = StrangeFileCategory.DOCUMENTS,
                labelRes = R.string.clean_cat_no_extension,
                iconRes = R.drawable.ic_home_family_24dp,
                color = "#FFD741".toColorInt()
            ),
            CategoryMeta(
                id = StrangeFileCategory.APK_CACHE,
                labelRes = R.string.clean_cat_apk_cache,
                iconRes = R.drawable.ic_home_boost_24dp,
                color = "#FF374B".toColorInt()
            )
        )
    }
}
