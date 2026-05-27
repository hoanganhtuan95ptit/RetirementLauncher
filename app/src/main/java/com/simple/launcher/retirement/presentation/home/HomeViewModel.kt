package com.simple.launcher.retirement.presentation.home

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.MemoryRepository
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.home.adapter.AppHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.CleanFilesHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.CleanMemoryHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.ClockHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.ContactHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.build
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import com.simple.launcher.retirement.utils.text.withStyleHeadlineMedium
import com.simple.launcher.retirement.utils.text.withStyleTitleLarge
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : BaseViewModel() {

    val countStrangeFiles = FileRepository.instance.countStrangeFilesFlow()

    val cleanFilesViewItemList: StateFlow<Pair<Double, List<ViewItem>>> = combineState(flow1 = resources, flow2 = countStrangeFiles, initialValue = 1.0 to emptyList()) { resources, fileCount ->

        val hasStrangeFiles = fileCount > 0

        val textColor = if (hasStrangeFiles) {
            resources.getColor(R.attr.colorCleanFilesStatCardOnBgActive)
        } else {
            resources.getColor(R.attr.colorCleanFilesStatCardOnBgIdle)
        }

        val backgroundColor = if (hasStrangeFiles) {
            resources.getColor(R.attr.colorCleanFilesStatCardBgActive)
        } else {
            resources.getColor(R.attr.colorCleanFilesStatCardBgIdle)
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
            resources.getColor(R.attr.colorCleanMemoryStatCardOnBgActive)
        } else {
            resources.getColor(R.attr.colorCleanMemoryStatCardOnBgIdle)
        }

        val backgroundColor = if (canCleanMemory) {
            resources.getColor(R.attr.colorCleanMemoryStatCardBgActive)
        } else {
            resources.getColor(R.attr.colorCleanMemoryStatCardBgIdle)
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

    val appAndContacts = GetHomeAppsUseCase.instance.asFlow()

    val appsAndContactsViewItemList: StateFlow<Pair<Double, List<ViewItem>>> = combineState(flow1 = resources, flow2 = appAndContacts, initialValue = 3.0 to emptyList()) { resources, entities ->

        val list = arrayListOf<ViewItem>()

        // Apps section
        val apps = entities.filterIsInstance<HomeContentEntity.App>()

        if (apps.isNotEmpty()) HeaderHomeItem(
            title = resources.getString(R.string.home_header_apps)
                .withStyleTitleLarge()
                .with(ForegroundColor(Color.WHITE))
                .build()
        ).let {
            list.add(it)
        }

        apps.map {
            it.toViewItem()
        }.let {
            list.addAll(it)
        }


        // Contacts section
        val contacts = entities.filterIsInstance<HomeContentEntity.Contact>()

        if (contacts.isNotEmpty()) HeaderHomeItem(
            title = resources.getString(R.string.home_header_contacts)
                .withStyleTitleLarge()
                .with(ForegroundColor(Color.WHITE))
                .build()
        ).let {
            list.add(it)
        }

        val textColor = resources.getColor(android.R.attr.textColorPrimary)
        val tapToCallLabel = resources.getString(R.string.contact_tap_to_call)

        contacts.map {

            it.toViewItem(textColor = textColor, tapToCallLabel = tapToCallLabel)
        }.let {
            list.addAll(it)
        }

        3.0 to list
    }


    val viewItemMap = MutableStateFlow<Map<Double, List<ViewItem>>>(mapOf(0.0 to listOf(ClockHomeItem)))

    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = cleanFilesViewItemList,
        flow2 = cleanMemoryViewItemList,
        flow3 = appsAndContactsViewItemList,
        flow4 = viewItemMap, initialValue = emptyList()
    ) { cleanFiles, cleanMemory, appsAndContacts, viewItemMap ->

        (listOf(cleanFiles, cleanMemory, appsAndContacts) + viewItemMap.toList())
            .sortedBy { it.first }
            .flatMap { it.second }
    }

    fun updateItem(order: Double, list: List<ViewItem>) {
        viewItemMap.value = viewItemMap.value.toMutableMap().apply {
            put(order, list)
        }
    }

    private fun HomeContentEntity.App.toViewItem() = AppHomeItem(
        entity = entity,
        icon = ImageDrawable(entity.icon),
        label = RichText(entity.label),
        background = Background.Builder()
            .backgroundColor(Color.WHITE)
            .cornerRadius(DP.DP_24)
            .build()
    )

    private fun HomeContentEntity.Contact.toViewItem(textColor: Int, tapToCallLabel: String) = ContactHomeItem(
        entity = entity,
        name = entity.name
            .with(ForegroundColor(textColor))
            .build(),
        photo = if (entity.photoUri != null) {
            ImagePath(entity.photoUri)
        } else {
            ImageRes(R.drawable.ic_home_contact_24dp)
        },
        background = Background.Builder()
            .backgroundColor(Color.WHITE)
            .cornerRadius(DP.DP_24)
            .build(),

        tapToCallLabel = tapToCallLabel
            .with(ForegroundColor(textColor))
            .build(),
        tapToCallBackground = Background.Builder()
            .backgroundColor("#F0F0F0".toColorInt())
            .cornerRadius(DP.DP_24)
            .build(),
    )
}
