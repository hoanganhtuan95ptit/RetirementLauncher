package com.simple.launcher.retirement.presentation.home

import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import com.simple.launcher.retirement.presentation.home.adapter.AppHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.CleanFilesHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.CleanMemoryHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.ClockHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.ContactHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
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

    // Mỗi nguồn data tự quản lý flow của mình.
    // ViewModel chỉ khai báo quan hệ giữa chúng — không init, không thủ công.
    val items: StateFlow<List<ViewItem>> = combine(
        getHomeAppsUseCase.asFlow(),
        repository.countStrangeFilesFlow(),
        repository.estimateCleanableMemoryMBFlow()
    ) { entities, fileCount, memoryMB ->
        buildList {
            add(ClockHomeItem)
            add(CleanFilesHomeItem(fileCount))
            add(CleanMemoryHomeItem(memoryMB))

            val apps = entities.filterIsInstance<HomeContentEntity.App>()
            if (apps.isNotEmpty()) {
                add(HeaderHomeItem(HEADER_QUICK_ACTIONS))
                addAll(apps.map { AppHomeItem(it.entity) })
            }

            val contacts = entities.filterIsInstance<HomeContentEntity.Contact>()
            if (contacts.isNotEmpty()) {
                add(HeaderHomeItem(HEADER_QUICK_CALLS))
                addAll(contacts.map { ContactHomeItem(it.entity) })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Yêu cầu repository phát lại giá trị mới cho các flow system status.
     * Dùng khi nhận broadcast FILE_CHANGED hoặc onResume.
     */
    fun loadSystemStatus() = repository.refreshSystemStatus()
}
