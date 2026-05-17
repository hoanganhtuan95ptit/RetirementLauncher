package com.simple.launcher.retirement.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Generic event bus dùng chung cho toàn app.
 *
 * Cách dùng:
 *   object MyEventBus : EventBus<MyType>()
 *
 *   // Gửi event (từ adapter / bất kỳ nơi nào):
 *   MyEventBus.post(item)
 *
 *   // Nhận event (từ Fragment / ViewModel):
 *   MyEventBus.events.collectLatest { item -> ... }
 */
open class EventBus<T> {

    private val _events = MutableSharedFlow<T>(extraBufferCapacity = 1)
    val events: SharedFlow<T> = _events.asSharedFlow()

    fun post(item: T) {
        _events.tryEmit(item)
    }
}

object AppEventBus : EventBus<AppEventBus.AppResult>() {

    sealed class AppResult

    sealed class PermissionResult: AppResult()

    object PermissionCancel : PermissionResult()

    object PermissionAccept : PermissionResult()
}
