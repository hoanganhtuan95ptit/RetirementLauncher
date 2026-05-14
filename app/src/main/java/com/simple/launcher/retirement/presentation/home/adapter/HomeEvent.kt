package com.simple.launcher.retirement.presentation.home.adapter

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object HomeEventBus {
    private val _events = MutableSharedFlow<HomeItem>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun post(item: HomeItem) {
        _events.tryEmit(item)
    }
}
