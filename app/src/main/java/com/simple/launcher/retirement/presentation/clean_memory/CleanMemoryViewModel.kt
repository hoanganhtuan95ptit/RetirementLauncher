package com.simple.launcher.retirement.presentation.clean_memory

/*class CleanMemoryViewModel : BaseViewModel() {

    var storageWhenStart: StorageInfo? = null

    val boostState = MutableStateFlow<BoostState>(BoostState.IDLE)

    val ramInfo: StateFlow<StorageInfo?> = boostState.filter {

        it !is BoostState.BOOSTING
    }.map {

        var storage = MemoryRepository.instance.getStorageInfo()

        if (it is BoostState.IDLE) {
            storageWhenStart = storage
        }

        val pre = storageWhenStart
        if (pre != null && pre.freeMB > storage.freeMB) {
            storage = storage.copy(usedMB = pre.usedMB, freeMB = pre.freeMB)
        }

        storage
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->
        val color = resources.textColorPrimary
        value = ToolbarState(
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

        val textColor = resourceMap.colorOnPrimary

        val backgroundColor = when (state) {
            BoostState.IDLE -> resourceMap.colorPrimary
            BoostState.BOOSTING -> resourceMap.colorPrimary.withAlpha(0.2f)
            is BoostState.Done -> resourceMap.colorPrimary
        }

        value = ActionState(
            text = labels
                .withStyleHeadlineSmall()
                .with(BigForegroundColor(textColor), BigBold)
                .build(),
            image = R.drawable.ic_boost_back_24dp.toBigImage(resourceMap.colorOnPrimary),
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

        value = RingViewData(
            value = "${ramInfo.percentInt}%\n${ramInfo.usedGB}/${ramInfo.totalGB}"
                .withStyleBodyLarge()
                .with(BigForegroundColor(resourceMap.colorOnSurfaceVariant))
                .withFirst("${ramInfo.percentInt}%", BigForegroundColor(resourceMap.colorPrimary), BigTextSize(28), BigBold)
                .build()
        )
    }

    val loadingViewData: StateFlow<LoadingViewData> = combineState(
        flow1 = boostState,
        flow2 = ramInfo.filterNotNull(),
        initialValue = LoadingViewData()
    ) { state, ramInfo ->

        value = LoadingViewData(
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
            .backgroundColor(resourceMap.colorSurface)
            .cornerRadius(DP.DP_16)
            .build()

        value = RamViewData(
            usedBigText = "${ramInfo.usedGB}\n${resourceMap.getString(R.string.clean_memory_stat_used)}"
                .withStyleBodyMedium()
                .with(BigForegroundColor(resourceMap.colorOnSurface))
                .withFirst(ramInfo.usedGB, BigTextSize(20), BigBold)
                .build(),
            usedBackground = background,

            freedBigText = "${ramInfo.freeGB}\n${resourceMap.getString(R.string.clean_memory_stat_free)}"
                .withStyleBodyMedium()
                .with(BigForegroundColor(resourceMap.colorOnSurface))
                .withFirst(ramInfo.freeGB, BigTextSize(20), BigBold)
                .build(),
            freedBackground = background,

            totalBigText = "${ramInfo.totalGB}\n${resourceMap.getString(R.string.clean_memory_stat_total)}"
                .withStyleBodyMedium()
                .with(BigForegroundColor(resourceMap.colorOnSurface))
                .withFirst(ramInfo.totalGB, BigTextSize(20), BigBold)
                .build(),
            totalBackground = background,
        )
    }

    val resultViewData: StateFlow<ResultViewData> = combineState(
        flow1 = resources,
        flow2 = boostState,
        initialValue = ResultViewData()
    ) { resourceMap, state ->

        val freeMB = state.asObjectOrNull<BoostState.Done>()?.freedMB ?: 0L
        val text = buildResultTitle(resourceMap, freeMB)

        val sub = resourceMap.getString(R.string.clean_memory_result_sub)

        value = ResultViewData(
            show = state is BoostState.Done,

            text = "$text\n$sub"
                .withStyleBodyMedium()
                .with(BigForegroundColor(resourceMap.colorOnSurface))
                .withFirst(text, BigTextSize(18), BigBold)
                .withFirst(freeMB.toString(), BigTextSize(24), BigForegroundColor(resourceMap.colorErrorContainer))
                .build(),

            image = R.drawable.ic_check_circle.toBigImage(),

            background = Background.Builder()
                .backgroundColor(resourceMap.colorPrimary.withAlpha(0.2f))
                .cornerRadius(DP.DP_16)
                .stroke(DP.DP_1, resourceMap.colorPrimary)
                .build()
        )
    }

    private fun buildResultTitle(resourceMap: Map<String, Any>, freeMB: Long): String {

        if (freeMB <= 0) {

            return resourceMap.getString(R.string.clean_memory_optimal)
        }

        return resourceMap.getString(R.string.clean_memory_toast)
            .replace("\$number_ram", freeMB.toString())
    }

    val screenViewData: StateFlow<ScreenViewData> = combineState(
        flow1 = ringViewData,
        flow2 = ramViewData,
        flow3 = resultViewData,
        initialValue = ScreenViewData()
    ) { ring, ram, result ->

        value = ScreenViewData(
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
        val value: BigText = emptyText()
    )

    data class LoadingViewData(
        val loading: Boolean = false,
        val percent: Float = 0f
    )

    data class RamViewData(
        val usedBigText: BigText = emptyText(),
        val usedBackground: Background = emptyBackground(),

        val freedBigText: BigText = emptyText(),
        val freedBackground: Background = emptyBackground(),

        val totalBigText: BigText = emptyText(),
        val totalBackground: Background = emptyBackground(),
    )

    data class ResultViewData(â
        val show: Boolean = false,

        val text: BigText = emptyText(),âd
        val image: BigImage = emptyImage(),

        val background: Background = emptyBackground()
    )

    data class ScreenViewData(
        val ringViewData: RingViewData = RingViewData(),
        val ramViewData: RamViewData = RamViewData(),
        val resultViewData: ResultViewData = ResultViewData()
    )
}*/
