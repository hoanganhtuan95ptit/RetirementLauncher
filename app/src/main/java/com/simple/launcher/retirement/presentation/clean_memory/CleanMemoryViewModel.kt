package com.simple.launcher.retirement.presentation.clean_memory

import android.graphics.Color
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.RamInfo
import com.simple.launcher.retirement.domain.repository.MemoryRepository
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.emptyBackground
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.getColor
import com.simple.launcher.retirement.utils.exts.getString
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanMemoryViewModel : BaseViewModel() {

    var ramWhenStart: RamInfo? = null

    val boostState = MutableStateFlow<BoostState>(BoostState.IDLE)

    val ramInfo: StateFlow<RamInfo?> = boostState.filter {

        it !is BoostState.BOOSTING
    }.map {

        var ram = MemoryRepository.instance.getRamInfo()

        if (it is BoostState.IDLE) {
            ramWhenStart = ram
        }

        val pre = ramWhenStart
        if (pre != null && pre.freeMB > ram.freeMB) {
            ram = ram.copy(usedMB = pre.usedMB, freeMB = pre.freeMB)
        }

        ram
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->
        val color = resources.getColor(android.R.attr.textColorPrimary)
        ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.clean_memory_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val action: StateFlow<ActionState> = combineState(flow1 = resources, flow2 = boostState, initialValue = ActionState.empty()) { resourceMap, state ->

        val labels = when (state) {
            BoostState.IDLE -> resourceMap.getString(R.string.clean_memory_start)
            BoostState.BOOSTING -> resourceMap.getString(R.string.clean_memory_running)
            is BoostState.Done -> resourceMap.getString(R.string.clean_memory_retry)
        }

        val textColor = resourceMap.getColor(com.google.android.material.R.attr.colorOnPrimary, Color.LTGRAY)

        val backgroundColor = when (state) {
            BoostState.IDLE -> resourceMap.getColor(android.R.attr.colorPrimary)
            BoostState.BOOSTING -> resourceMap.getColor(android.R.attr.colorPrimary).withAlpha(0.2f)
            is BoostState.Done -> resourceMap.getColor(android.R.attr.colorPrimary)
        }

        ActionState(
            text = labels
                .withStyleHeadlineSmall()
                .with(ForegroundColor(textColor), Bold)
                .build(),
            image = ImageRes(data = R.drawable.ic_boost_back_24dp, colorFilter = resourceMap.getColor(com.google.android.material.R.attr.colorOnPrimary)),
            imageShow = true,

            background = Background.Builder()
                .backgroundColor(backgroundColor)
                .cornerRadius(DP.DP_24)
                .build()
        )
    }

    val ringViewData: StateFlow<RingViewData> = combineState(
        flow1 = resources,
        flow2 = ramInfo.filterNotNull(),
        initialValue = RingViewData()
    ) { resourceMap, ramInfo ->

        RingViewData(
            value = "${ramInfo.percentInt}%\n${ramInfo.usedGB}/${ramInfo.totalGB}"
                .withStyleBodyLarge()
                .with(ForegroundColor(resourceMap.getColor(com.google.android.material.R.attr.colorOnSurfaceVariant)))
                .withFirst("${ramInfo.percentInt}%", ForegroundColor(resourceMap.getColor(android.R.attr.colorPrimary)), TextSize(28), Bold)
                .build()
        )
    }

    val loadingViewData: StateFlow<LoadingViewData> = combineState(
        flow1 = boostState,
        flow2 = ramInfo.filterNotNull(),
        initialValue = LoadingViewData()
    ) { state, ramInfo ->

        LoadingViewData(
            loading = state is BoostState.BOOSTING,
            percent = ramInfo.percent,
        )
    }

    val ramViewData: StateFlow<RamViewData> = combineState(
        flow1 = resources,
        flow2 = ramInfo.filterNotNull(),
        initialValue = RamViewData()
    ) { resourceMap, ramInfo ->

        val background = Background.Builder()
            .backgroundColor(resourceMap.getColor(com.google.android.material.R.attr.colorSurface))
            .cornerRadius(DP.DP_16)
            .build()

        RamViewData(
            usedRichText = "${ramInfo.usedGB}\n${resourceMap.getString(R.string.clean_memory_stat_used)}"
                .withStyleBodyMedium()
                .with(ForegroundColor(resourceMap.getColor(com.google.android.material.R.attr.colorOnSurface)))
                .withFirst(ramInfo.usedGB, TextSize(20), Bold)
                .build(),
            usedBackground = background,

            freedRichText = "${ramInfo.freeGB}\n${resourceMap.getString(R.string.clean_memory_stat_free)}"
                .withStyleBodyMedium()
                .with(ForegroundColor(resourceMap.getColor(com.google.android.material.R.attr.colorOnSurface)))
                .withFirst(ramInfo.freeGB, TextSize(20), Bold)
                .build(),
            freedBackground = background,

            totalRichText = "${ramInfo.totalGB}\n${resourceMap.getString(R.string.clean_memory_stat_total)}"
                .withStyleBodyMedium()
                .with(ForegroundColor(resourceMap.getColor(com.google.android.material.R.attr.colorOnSurface)))
                .withFirst(ramInfo.totalGB, TextSize(20), Bold)
                .build(),
            totalBackground = background,
        )
    }

    val resultViewData: StateFlow<ResultViewData> = combineState(
        flow1 = resources,
        flow2 = boostState,
        initialValue = ResultViewData()
    ) { resourceMap, state ->

        val freeMB by lazy {
            state.asObjectOrNull<BoostState.Done>()?.freedMB ?: 0L
        }

        val text = if (freeMB > 0) {
            resourceMap.getString(R.string.clean_memory_toast)
                .replace("\$\$number_ram", freeMB.toString())
        } else {

            resourceMap.getString(R.string.clean_memory_optimal)
        }

        val sub = resourceMap.getString(R.string.clean_memory_result_sub)

        ResultViewData(
            show = state is BoostState.Done,

            text = "$text\n$sub"
                .withStyleBodyMedium()
                .with(ForegroundColor(resourceMap.getColor(com.google.android.material.R.attr.colorOnSurface)))
                .withFirst(text, TextSize(18), Bold)
                .withFirst(freeMB.toString(), TextSize(24), ForegroundColor(resourceMap.getColor(com.google.android.material.R.attr.colorErrorContainer)))
                .build(),

            image = ImageRes(R.drawable.ic_check_circle),

            background = Background.Builder()
                .backgroundColor(resourceMap.getColor(android.R.attr.colorPrimary).withAlpha(0.2f))
                .cornerRadius(DP.DP_16)
                .stroke(DP.DP_1, resourceMap.getColor(android.R.attr.colorPrimary))
                .build()
        )
    }

    val screenViewData: StateFlow<ScreenViewData> = combineState(
        flow1 = ringViewData,
        flow2 = ramViewData,
        flow3 = resultViewData,
        initialValue = ScreenViewData()
    ) { ring, ram, result ->

        ScreenViewData(
            ringViewData = ring,
            ramViewData = ram,
            resultViewData = result
        )
    }

    fun startBoost() = viewModelScope.launch {

        if (boostState.value is BoostState.BOOSTING) return@launch

        boostState.value = BoostState.BOOSTING

        val freedBytes = withContext(Dispatchers.IO) {
            val result = MemoryRepository.instance.cleanMemory()
            delay(2000)
            result
        }

        boostState.value = BoostState.Done(freedBytes / (1024 * 1024))
    }

    sealed class BoostState {

        object IDLE : BoostState()

        object BOOSTING : BoostState()

        data class Done(val freedMB: Long) : BoostState()
    }

    data class RingViewData(
        val value: RichText = emptyText()
    )

    data class LoadingViewData(
        val loading: Boolean = false,
        val percent: Float = 0f
    )

    data class RamViewData(
        val usedRichText: RichText = emptyText(),
        val usedBackground: Background = emptyBackground(),

        val freedRichText: RichText = emptyText(),
        val freedBackground: Background = emptyBackground(),

        val totalRichText: RichText = emptyText(),
        val totalBackground: Background = emptyBackground(),
    )

    data class ResultViewData(
        val show: Boolean = false,

        val text: RichText = emptyText(),
        val image: RichImage = emptyImage(),

        val background: Background = emptyBackground()
    )

    data class ScreenViewData(
        val ringViewData: RingViewData = RingViewData(),
        val ramViewData: RamViewData = RamViewData(),
        val resultViewData: ResultViewData = ResultViewData()
    )
}
