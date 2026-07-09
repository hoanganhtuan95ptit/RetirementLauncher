package com.simple.launcher.retirement.utils

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Generic event bus base class.
 * Dùng [AppEventBus] (định nghĩa trong AppEvent.kt) để gửi / nhận toàn bộ event của app.
 */
open class EventBus<T> {

    private val _events = MutableSharedFlow<T>(replay = 0, extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)
    val events: Flow<T> = _events

    fun post(item: T) {
        _events.tryEmit(item)
    }
}
