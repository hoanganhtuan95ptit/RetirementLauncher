package com.simple.launcher.retirement.presentation.home

import android.graphics.Color
import android.graphics.Typeface
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
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.CustomFont
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val getHomeAppsUseCase: GetHomeAppsUseCase,
    private val fileRepository: FileRepository,
    private val memoryRepository: MemoryRepository
) : BaseViewModel() {

    val cleanFilesHomeItem: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
        flow1 = strings,
        flow2 = fileRepository.countStrangeFilesFlow(),
        initialValue = 1.0 to emptyList()
    ) { strings, fileCount ->

        val fileCountLabel = "$fileCount"
        val list = arrayListOf<ViewItem>()

        CleanFilesHomeItem(
            label = RichText.Builder(strings.getString(R.string.home_strange_files)
                .replace("\$file_number", fileCountLabel))
                .withFirst(fileCountLabel, Bold)
                .build(),
            icon = ImageRes(R.drawable.img_home_clean_up)
        ).let { list.add(it) }

        1.0 to list
    }

    val cleanMemoryHomeItem: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
        flow1 = strings,
        flow2 = memoryRepository.estimateCleanableMemoryMBFlow(),
        initialValue = 2.0 to emptyList()
    ) { strings, memoryMB ->

        val memoryLabel = "$memoryMB MB"
        val list = arrayListOf<ViewItem>()

        CleanMemoryHomeItem(
            label = RichText.Builder(strings.getString(R.string.home_memory_status)
                .replace("\$memory_mb", memoryLabel))
                .withFirst(memoryLabel, Bold)
                .build(),
            icon = ImageRes(R.drawable.img_home_boost)
        ).let { list.add(it) }

        2.0 to list
    }

    // Gộp appHomeItems + contactHomeItems thành một flow duy nhất subscribe getHomeAppsUseCase.asFlow().
    // Trước đây: hai flow riêng → invoke() chạy 2 lần mỗi trigger (dù đã fix shareIn ở usecase).
    // Bây giờ: một combine duy nhất, tách apps/contacts bên trong transform.
    val appsAndContactsHomeItems: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = getHomeAppsUseCase.asFlow(),
        initialValue = 3.0 to emptyList()
    ) { strings, themeMap, entities ->
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary)
        val list = arrayListOf<ViewItem>()

        // Apps section
        val apps = entities.filterIsInstance<HomeContentEntity.App>()
        if (apps.isNotEmpty()) {
            HeaderHomeItem(
                RichText.Builder(strings.getString(R.string.home_header_apps))
                    .with(ForegroundColor(Color.WHITE), TextSize(20), CustomFont(Typeface.create("sans-serif-medium", Typeface.NORMAL)))
                    .build()
            ).let { list.add(it) }

            apps.map { AppHomeItem(RichText.Builder(it.entity.label).build(), ImageDrawable(it.entity.icon), it.entity) }
                .let { list.addAll(it) }
        }

        // Contacts section
        val contacts = entities.filterIsInstance<HomeContentEntity.Contact>()
        if (contacts.isNotEmpty()) {
            HeaderHomeItem(
                RichText.Builder(strings.getString(R.string.home_header_contacts))
                    .with(ForegroundColor(Color.WHITE), TextSize(20), CustomFont(Typeface.create("sans-serif-medium", Typeface.NORMAL)))
                    .build()
            ).let { list.add(it) }

            contacts.map { contact ->
                val photo = if (contact.entity.photoUri != null) {
                    ImagePath(contact.entity.photoUri)
                } else {
                    ImageRes(R.drawable.ic_home_contact_24dp)
                }
                ContactHomeItem(
                    name = RichText.Builder(contact.entity.name)
                        .with(ForegroundColor(textColor))
                        .build(),
                    tapToCallLabel = RichText.Builder(strings.getString(R.string.contact_tap_to_call))
                        .with(ForegroundColor(textColor))
                        .build(),
                    photo = photo,
                    entity = contact.entity
                )
            }.let { list.addAll(it) }
        }

        3.0 to list
    }

    private val _itemMap = MutableStateFlow<Map<Double, List<ViewItem>>>(
        mapOf(0.0 to listOf(ClockHomeItem))
    )

    // 4 flows thay vì 5 — gộp apps+contacts thành một
    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = cleanFilesHomeItem,
        flow2 = cleanMemoryHomeItem,
        flow3 = appsAndContactsHomeItems,
        flow4 = _itemMap,
        initialValue = emptyList()
    ) { cleanFiles, cleanMemory, appsAndContacts, extraMap ->
        (extraMap.toList() + listOf(cleanFiles, cleanMemory, appsAndContacts))
            .sortedBy { it.first }
            .flatMap { it.second }
    }

    fun loadSystemStatus() {
//        fileRepository.refreshFileStatus()
        memoryRepository.refreshMemoryStatus()
    }

    fun updateItem(order: Double, list: List<ViewItem>) {
        _itemMap.value = _itemMap.value.toMutableMap().apply {
            put(order, list)
        }
    }
}
