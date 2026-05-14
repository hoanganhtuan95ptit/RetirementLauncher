package com.simple.launcher.retirement.presentation.settings

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SettingsEventBus {
    private val _events = MutableSharedFlow<SettingItem>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun post(item: SettingItem) {
        _events.tryEmit(item)
    }
}
