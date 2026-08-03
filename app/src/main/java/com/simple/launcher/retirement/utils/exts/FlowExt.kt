package com.simple.launcher.retirement.utils.exts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@OptIn(InternalCoroutinesApi::class)
open class ActiveStateFlow<T>(
    def: T,
    private val source: MutableStateFlow<T> = MutableStateFlow(def),
) : MutableStateFlow<T> by source {

    private val count = AtomicInteger(0)

    @Volatile
    private var _scope: CoroutineScope? = null

    protected val scope: CoroutineScope
        get() = _scope ?: error("scope chỉ khả dụng trong onActive/onInactive")

    protected open suspend fun onActive() {}
    protected open suspend fun onInactive() {}

    override suspend fun collect(collector: FlowCollector<T>): Nothing = coroutineScope {
        if (count.incrementAndGet() == 1) {
            val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            _scope = newScope
            newScope.launch { onActive() }
        }
        try {
            source.collect(collector)
        } finally {
            if (count.decrementAndGet() == 0) {
                val toCancel = _scope
                withContext(NonCancellable) { onInactive() }
                toCancel?.cancel()
                _scope = null
            }
        }
    }
}

fun <T> mutableStateFlow(
    def: T,
    action: suspend MutableStateFlow<T>.() -> Unit
): MutableStateFlow<T> = object : ActiveStateFlow<T>(def) {
    override suspend fun onActive() { action(this) }
}