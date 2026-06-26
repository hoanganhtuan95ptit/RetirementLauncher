package com.simple.launcher.retirement.presentation.home

import com.simple.launcher.retirement.presentation.base.ViewItemViewModel

class HomeViewModel : ViewItemViewModel() {

    /*
        val countStrangeFiles = FileRepository.instance.countStrangeFilesFlow()

        val cleanFilesViewItemList: StateFlow<Pair<Double, List<ViewItem>>> = combineState(flow1 = resources, flow2 = countStrangeFiles, initialValue = 1.0 to emptyList()) { resources, fileCount ->

            val hasStrangeFiles = fileCount > 0

            val textColor = if (hasStrangeFiles) {
                resources.colorCleanFilesStatCardOnBgActive
            } else {
                resources.colorCleanFilesStatCardOnBgIdle
            }

            val backgroundColor = if (hasStrangeFiles) {
                resources.colorCleanFilesStatCardBgActive
            } else {
                resources.colorCleanFilesStatCardBgIdle
            }

            1.0 to CleanFilesHomeItem(
                label = resources.getString(R.string.clean_files_title)
                    .withStyleBodyLarge()
                    .with(ForegroundColor(textColor))
                    .build(),
                value = "$fileCount"
                    .withStyleHeadlineMedium()
                    .with(ForegroundColor(textColor), Bold)
                    .build(),
                icon = ImageRes(R.drawable.ic_clear_files_black_24dp),
                background = Background.Builder()
                    .backgroundColor(backgroundColor)
                    .cornerRadius(DP.DP_16)
                    .build()
            ).let {

                listOf(it)
            }
        }


        val estimateCleanableMemory = MemoryRepository.instance.estimateCleanableMemoryMBFlow()

        val cleanMemoryViewItemList: StateFlow<Pair<Double, List<ViewItem>>> = combineState(flow1 = resources, flow2 = estimateCleanableMemory, initialValue = 2.0 to emptyList()) { resources, memoryMB ->

            val memoryLabel = "$memoryMB MB"
            val canCleanMemory = memoryMB > 0

            val textColor = if (canCleanMemory) {
                resources.colorCleanMemoryStatCardOnBgActive
            } else {
                resources.colorCleanMemoryStatCardOnBgIdle
            }

            val backgroundColor = if (canCleanMemory) {
                resources.colorCleanMemoryStatCardBgActive
            } else {
                resources.colorCleanMemoryStatCardBgIdle
            }

            2.0 to CleanMemoryHomeItem(
                label = resources.getString(R.string.clean_memory_title)
                    .withStyleBodyLarge()
                    .with(ForegroundColor(textColor))
                    .build(),
                value = memoryLabel
                    .withStyleHeadlineMedium()
                    .with(ForegroundColor(textColor), Bold)
                    .build(),
                icon = ImageRes(R.drawable.ic_boost_back_24dp),
                background = Background.Builder()
                    .backgroundColor(backgroundColor)
                    .cornerRadius(DP.DP_16)
                    .build()
            ).let {

                listOf(it)
            }
        }
    */
}
