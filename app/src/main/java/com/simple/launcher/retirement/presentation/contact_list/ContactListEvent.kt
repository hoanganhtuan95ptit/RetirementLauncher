package com.simple.launcher.retirement.presentation.contact_list

import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ContactListEventBus {
    private val _events = MutableSharedFlow<SelectableContactEntity>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun post(entity: SelectableContactEntity) {
        _events.tryEmit(entity)
    }
}
