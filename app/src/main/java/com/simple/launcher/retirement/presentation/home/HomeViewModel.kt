package com.simple.launcher.retirement.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simple.launcher.retirement.domain.model.HomeItem
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase

class HomeViewModel(private val getHomeAppsUseCase: GetHomeAppsUseCase) : ViewModel() {

    private val _items = MutableLiveData<List<HomeItem>>()
    val items: LiveData<List<HomeItem>> = _items

    fun loadApps() {
        _items.value = getHomeAppsUseCase()
    }
}
