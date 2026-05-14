package com.simple.launcher.retirement.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val getHomeAppsUseCase: GetHomeAppsUseCase,
    private val repository: AppRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<ViewItem>>()
    val items: LiveData<List<ViewItem>> = _items

    private var baseEntities: List<HomeContentEntity> = emptyList()
    private var strangeFilesCount: Int = 0
    private var cleanableMemoryMB: Long = 0

    fun loadApps() {
        viewModelScope.launch {
            baseEntities = withContext(Dispatchers.IO) {
                getHomeAppsUseCase()
            }
            updateItems()
        }
    }

    fun loadSystemStatus() {
        viewModelScope.launch {
            strangeFilesCount = withContext(Dispatchers.IO) {
                repository.countStrangeFiles()
            }
            cleanableMemoryMB = withContext(Dispatchers.IO) {
                repository.estimateCleanableMemory() / (1024 * 1024)
            }
            updateItems()
        }
    }

    private fun updateItems() {
        val allItems = mutableListOf<ViewItem>()
        allItems.add(ClockHomeItem)
        allItems.add(CleanFilesHomeItem(strangeFilesCount))
        allItems.add(CleanMemoryHomeItem(cleanableMemoryMB))

        val apps = baseEntities.filterIsInstance<HomeContentEntity.App>()
        if (apps.isNotEmpty()) {
            allItems.add(HeaderHomeItem("Quick Actions"))
            allItems.addAll(apps.map { AppHomeItem(it.entity) })
        }

        val contacts = baseEntities.filterIsInstance<HomeContentEntity.Contact>()
        if (contacts.isNotEmpty()) {
            allItems.add(HeaderHomeItem("Quick Calls"))
            allItems.addAll(contacts.map { ContactHomeItem(it.entity) })
        }

        _items.value = allItems
    }
}
