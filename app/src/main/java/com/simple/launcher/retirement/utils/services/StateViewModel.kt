package com.simple.launcher.retirement.utils.services

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel

enum class LifecycleState(val value: Int) {
    Attached(1), Created(2), ViewCreated(3), Started(4), Resumed(5), Paused(6), Stopped(7), ViewDestroyed(8), Destroyed(9),
}

class LifecycleStateViewModel : ViewModel() {

    val lifecycleState = MediatorLiveData<LifecycleState>()

    fun updateState(lifecycleState: LifecycleState) {

        this.lifecycleState.value = lifecycleState
    }
}