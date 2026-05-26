package com.simple.launcher.retirement.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.MemoryRepository
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase

class HomeViewModelFactory(
    private val getHomeAppsUseCase: GetHomeAppsUseCase,
    private val fileRepository: FileRepository,
    private val memoryRepository: MemoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
