package com.simple.launcher.retirement.presentation.home.services.time

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimeViewModel : BaseViewModel() {

    val timeViewItemList: StateFlow<Pair<Int, List<ViewItem>>> = MutableStateFlow(0 to listOf(ClockHomeItem))
}