package com.simple.launcher.retirement.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.domain.model.HomeItem
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val getHomeAppsUseCase: GetHomeAppsUseCase,
    private val repository: AppRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<HomeItem>>()
    val items: LiveData<List<HomeItem>> = _items

    private var baseItems: List<HomeItem> = emptyList()
    private var strangeFilesCount: Int = 0
    private var cleanableMemoryMB: Long = 0

    fun loadApps() {
        viewModelScope.launch {
            baseItems = withContext(Dispatchers.IO) {
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
        val allItems = mutableListOf<HomeItem>()
        allItems.add(HomeItem.Clock)
        allItems.add(HomeItem.CleanFiles(strangeFilesCount))
        allItems.add(HomeItem.CleanMemory(cleanableMemoryMB))
        allItems.addAll(baseItems)
        _items.value = allItems
    }
}
