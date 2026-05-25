package com.simple.launcher.retirement.presentation.worker

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BackgroundWorker(protected val context: Context) {

    protected var scope: CoroutineScope? = null
        private set

    abstract fun observeEnabled(): Flow<Boolean>

    protected abstract fun onStart()

    protected abstract fun onStop()

    fun attach(scope: CoroutineScope) {
        this.scope = scope
        scope.launch {
            observeEnabled().collect { isEnabled ->
                if (isEnabled) onStart() else onStop()
            }
        }
    }

    fun detach() {
        onStop()
        scope = null
    }
}
