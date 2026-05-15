package com.simple.launcher.retirement.presentation.home

import android.graphics.Color
import android.graphics.Typeface
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
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
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.text.withFirst
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val getHomeAppsUseCase: GetHomeAppsUseCase,
    private val repository: AppRepository
) : BaseViewModel() {

    val cleanFilesHomeItem: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
        flow1 = strings,
        flow2 = repository.countStrangeFilesFlow(),
        initialValue = 1.0 to emptyList()
    ) { strings, fileCount ->

        val fileCountLabel = "$fileCount"

        val list = arrayListOf<ViewItem>()

        CleanFilesHomeItem(
            label = strings.getString(R.string.home_strange_files)
                .replace("\$file_number", fileCountLabel)
                .withFirst(fileCountLabel, Bold),
            icon = ImageRes(R.drawable.img_home_clean_up)
        ).let {

            list.add(it)
        }

        1.0 to list
    }

    val cleanMemoryHomeItem: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
        flow1 = strings,
        flow2 = repository.estimateCleanableMemoryMBFlow(),
        initialValue = 2.0 to emptyList()
    ) { strings, memoryMB ->

        val memoryLabel = "$memoryMB MB"

        val list = arrayListOf<ViewItem>()

        CleanMemoryHomeItem(
            label = strings.getString(R.string.home_memory_status)
                .replace("\$memory_mb", memoryLabel)
                .withFirst(memoryLabel, Bold),
            icon = ImageRes(R.drawable.img_home_boost)
        ).let {
            list.add(it)
        }

        2.0 to list
    }

    val appHomeItems: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = getHomeAppsUseCase.asFlow(),
        initialValue = 3.0 to emptyList()
    ) { strings, themes, entities ->
        val apps = entities.filterIsInstance<HomeContentEntity.App>()
        val list = arrayListOf<ViewItem>()
        if (apps.isEmpty()) return@combineState 3.0 to list

        HeaderHomeItem(
            strings.getString(R.string.home_header_apps).toRich().with(
                ForegroundColor(Color.WHITE),
                TextSize(20),
                CustomFont(Typeface.create("sans-serif-medium", Typeface.NORMAL))
            )
        ).let {
            list.add(it)
        }

        apps.map { AppHomeItem(it.entity.label.toRich(), ImageDrawable(it.entity.icon), it.entity) }.let {
            list.addAll(it)
        }

        3.0 to list
    }

    val contactHomeItems: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = getHomeAppsUseCase.asFlow(),
        initialValue = 4.0 to emptyList()
    ) { strings, themes, entities ->
        val contacts = entities.filterIsInstance<HomeContentEntity.Contact>()
        val list = arrayListOf<ViewItem>()
        if (contacts.isEmpty()) return@combineState 4.0 to list

        HeaderHomeItem(
            strings.getString(R.string.home_header_contacts).toRich().with(
                ForegroundColor(Color.WHITE),
                TextSize(20),
                CustomFont(Typeface.create("sans-serif-medium", Typeface.NORMAL))
            )
        ).let {
            list.add(it)
        }

        contacts.map { contact ->
            val photo = if (contact.entity.photoUri != null) {
                ImagePath(contact.entity.photoUri)
            } else {
                ImageRes(R.drawable.ic_home_contact_24dp)
            }
            ContactHomeItem(contact.entity.name.toRich(), photo, contact.entity)
        }.let {
            list.addAll(it)
        }

        4.0 to list
    }

    private val _itemMap = MutableStateFlow<Map<Double, List<ViewItem>>>(
        mapOf(0.0 to listOf(ClockHomeItem))
    )

    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = cleanFilesHomeItem,
        flow2 = cleanMemoryHomeItem,
        flow3 = appHomeItems,
        flow4 = contactHomeItems,
        flow5 = _itemMap,
        initialValue = emptyList()
    ) { cleanFiles, cleanMemory, apps, contacts, extraMap ->
        (extraMap.toList() + listOf(cleanFiles, cleanMemory, apps, contacts))
            .sortedBy { it.first }
            .flatMap { it.second }
    }

    /**
     * Yêu cầu repository phát lại giá trị mới cho các flow system status.
     * Dùng khi nhận broadcast FILE_CHANGED hoặc onResume.
     */
    fun loadSystemStatus() = repository.refreshSystemStatus()
    
    fun updateItem(order: Double, list: List<ViewItem>) {
        _itemMap.value = _itemMap.value.toMutableMap().apply {
            put(order, list)
        }
    }
}
