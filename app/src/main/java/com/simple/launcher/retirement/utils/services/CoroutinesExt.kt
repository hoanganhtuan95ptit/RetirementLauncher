package com.simple.launcher.retirement.utils.services

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal fun <T> Flow<T>.launchCollect(lifecycleOwner: LifecycleOwner, action: suspend (T) -> Unit) = lifecycleOwner.lifecycleScope.launch {
    collect { data ->
        action(data)
    }
}
