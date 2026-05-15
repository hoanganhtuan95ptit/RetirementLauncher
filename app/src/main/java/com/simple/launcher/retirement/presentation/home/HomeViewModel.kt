package com.simple.launcher.retirement.presentation.home

import androidx.lifecycle.viewModelScope
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
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.withFirst
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val getHomeAppsUseCase: GetHomeAppsUseCase,
    private val repository: AppRepository
) : BaseViewModel() {

    companion object {
        private const val HEADER_QUICK_ACTIONS = "Quick Actions"
        private const val HEADER_QUICK_CALLS = "Quick Calls"
    }

    val items: StateFlow<List<ViewItem>> = combine(
        getHomeAppsUseCase.asFlow(),
        repository.countStrangeFilesFlow(),
        repository.estimateCleanableMemoryMBFlow()
    ) { entities, fileCount, memoryMB ->
        buildList {
            add(ClockHomeItem)

            val fileCountLabel = "($fileCount)"
            add(CleanFilesHomeItem(
                label = "Clean up $fileCountLabel".withFirst(fileCountLabel, Bold),
                icon = ImageRes(R.drawable.ic_home_cleanup_24dp)
            ))

            val memoryLabel = "($memoryMB)"
            add(CleanMemoryHomeItem(
                label = "Boost $memoryLabel".withFirst(memoryLabel, Bold),
                icon = ImageRes(android.R.drawable.ic_lock_power_off)
            ))

            val apps = entities.filterIsInstance<HomeContentEntity.App>()
            if (apps.isNotEmpty()) {
                add(HeaderHomeItem(HEADER_QUICK_ACTIONS.toRich()))
                addAll(apps.map { AppHomeItem(it.entity.label.toRich(), ImageDrawable(it.entity.icon), it.entity) })
            }

            val contacts = entities.filterIsInstance<HomeContentEntity.Contact>()
            if (contacts.isNotEmpty()) {
                add(HeaderHomeItem(HEADER_QUICK_CALLS.toRich()))
                addAll(contacts.map { contact ->
                    val photo = if (contact.entity.photoUri != null) {
                        ImagePath(contact.entity.photoUri)
                    } else {
                        ImageRes(R.drawable.ic_home_contact_24dp)
                    }
                    ContactHomeItem(contact.entity.name.toRich(), photo, contact.entity)
                })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Yêu cầu repository phát lại giá trị mới cho các flow system status.
     * Dùng khi nhận broadcast FILE_CHANGED hoặc onResume.
     */
    fun loadSystemStatus() = repository.refreshSystemStatus()
}
