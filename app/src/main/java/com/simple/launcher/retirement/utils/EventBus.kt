package com.simple.launcher.retirement.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Generic event bus base class.
 * Dùng [AppEventBus] (định nghĩa trong AppEvent.kt) để gửi / nhận toàn bộ event của app.
 */
open class EventBus<T> {

    private val _events = MutableSharedFlow<T>(extraBufferCapacity = 1)
    val events: SharedFlow<T> = _events.asSharedFlow()

    fun post(item: T) {
        _events.tryEmit(item)
    }
}
