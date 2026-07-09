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
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.image.emptyImage
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.build
import com.simple.launcher.retirement.utils.text.emptyText
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.text.withFirst
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import com.simple.launcher.retirement.utils.text.withStyleBodyMedium
import com.simple.launcher.retirement.utils.text.withStyleHeadlineSmall
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
                .with(ForegroundColor(textColor), Bold)
                .build(),

            image = ImageRes(data = R.drawable.ic_clear_files_black_24dp, colorFilter = resourceMap.colorOnPrimary),
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
                .with(ForegroundColor(secondaryColor))
                .withFirst(count.toString(), ForegroundColor(primaryColor), TextSize(26), Bold)
                .build()
        ) else if (state is ClearState.IDLE || state is ClearState.Scanning) RingViewData(
            showIcon = true,
            icon = ImageRes(R.drawable.ic_clear_files_black_24dp, resources.colorPrimary),
        ) else RingViewData(
            text = (resultStr + "\n" + resources.getString(R.string.clean_result_files_deleted))
                .withStyleBodyLarge()
                .with(ForegroundColor(secondaryColor))
                .withFirst(resultStr, ForegroundColor(primaryColor), TextSize(26), Bold)
                .build(),
        )
    }

    val statusText: StateFlow<RichText> = combineState(flow1 = resources, flow2 = screenState, flow3 = strangeFileCount, initialValue = emptyText()) { resources, state, count ->

        val color = resources.textColorSecondary

        val text = if (state is ClearState.IDLE) if (count > 0) {

            resources.getString(R.string.clean_files_idle_desc).replace("\$number_file", "$count")
        } else {

            resources.getString(R.string.clean_files_desc)
        } else if (state is ClearState.Scanning) {

            resources.getString(R.string.clean_files_running)
        } else {

            resources.getString(R.string.clean_files_completed)
        }

        value = text
            .with(ForegroundColor(color))
            .build()
    }

    val categoryViewDataList: StateFlow<List<CategoryViewData>> = combineState(flow1 = resources, flow2 = screenState, initialValue = emptyList()) { resources, state ->

        value = CATEGORY_META.map {

            val numberFile = if (state is ClearState.Run && state.categoryMap[it.id] != null) {

                resources.getString(R.string.clean_cat_file_count).replace("\$number_file","${state.categoryMap[it.id]}")
            } else {

                ""
            }

            val showSelected = if (state is ClearState.Run) {

                state.categoryMap[it.id] != null
            } else {

                false
            }

            CategoryViewData(
                image = ImageRes(it.iconRes, colorFilter = it.color),
                imageBackground = Background.Builder()
                    .backgroundColor(it.color.withAlpha(0.2f))
                    .cornerRadius(DP.DP_8)
                    .build(),
                label = resources.getString(it.labelRes)
                    .withStyleBodyLarge()
                    .with(ForegroundColor(resources.colorOnSurface))
                    .build(),
                numberFile = numberFile
                    .withStyleBodyMedium()
                    .with(ForegroundColor(resources.colorOnSurface))
                    .build(),
                showSelected = showSelected
            )
        }
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
                    .with(ForegroundColor(res.colorOnSurface))
                    .build(),

                resultFilesImage = ImageRes(R.drawable.ic_file_black_24dp, "#FFBB00".toColorInt()),
                resultFilesLabel = "${state.totalFiles.orZero()}\n${res.getString(R.string.clean_result_files_deleted)}"
                    .withStyleBodyMedium()
                    .with(ForegroundColor(res.colorOnSurface))
                    .withFirst("${state.totalFiles.orZero()}", TextSize(24), Bold)
                    .build(),
                resultFilesBackground = Background.Builder()
                    .backgroundColor(res.colorSurface)
                    .cornerRadius(DP.DP_24)
                    .build(),

                resultSpaceImage = ImageRes(R.drawable.ic_clear_files_black_24dp, "#FF4343".toColorInt()),
                resultSpaceLabel = "${spaceLabel}\n${res.getString(R.string.clean_result_space_freed)}"
                    .withStyleBodyMedium()
                    .with(ForegroundColor(res.colorOnSurface))
                    .withFirst(spaceLabel, TextSize(24), Bold)
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
        val icon: RichImage = emptyImage(),
        val text: RichText = emptyText(),
        val showIcon: Boolean = false,
    )

    data class CategoryViewData(
        val image: RichImage = emptyImage(),
        val imageBackground: Background = Background(),

        val label: RichText = emptyText(),
        val numberFile: RichText = emptyText(),

        val showSelected: Boolean = false
    )

    data class ResultViewData(
        val show: Boolean = false,

        val title: RichText = emptyText(),

        val resultFilesLabel: RichText = emptyText(),
        val resultFilesImage: RichImage = emptyImage(),
        val resultFilesBackground: Background = Background(),

        val resultSpaceLabel: RichText = emptyText(),
        val resultSpaceImage: RichImage = emptyImage(),
        val resultSpaceBackground: Background = Background(),
    )

    data class ScreenViewData(
        val ringViewData: RingViewData = RingViewData(),
        val status: RichText = emptyText(),
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
