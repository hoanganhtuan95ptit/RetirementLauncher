package com.simple.launcher.retirement.presentation.app_list

import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppListEventBus {
    private val _events = MutableSharedFlow<SelectableAppEntity>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun post(entity: SelectableAppEntity) {
        _events.tryEmit(entity)
    }
}
