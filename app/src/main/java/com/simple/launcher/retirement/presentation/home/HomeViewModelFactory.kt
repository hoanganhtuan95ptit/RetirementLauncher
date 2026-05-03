package com.simple.launcher.retirement.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase

class HomeViewModelFactory(private val getHomeAppsUseCase: GetHomeAppsUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(getHomeAppsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
