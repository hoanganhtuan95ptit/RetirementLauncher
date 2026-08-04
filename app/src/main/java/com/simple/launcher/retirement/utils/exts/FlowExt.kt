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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * MutableStateFlow tự động gọi [onActive] khi có collector đầu tiên và [onInactive]
 * khi collector cuối rời đi. Toàn bộ transition được bảo vệ bằng [transitionMutex]
 * để tránh race giữa unsubscribe (đang dọn scope) và subscribe mới (đang tạo scope).
 */
@OptIn(InternalCoroutinesApi::class)
open class ActiveStateFlow<T>(
    def: T,
    private val source: MutableStateFlow<T> = MutableStateFlow(def),
) : MutableStateFlow<T> by source {

    // ── 1. Fields ─────────────────────────────────────────────────────────
    private val transitionMutex = Mutex()
    private var count: Int = 0
    private var _scope: CoroutineScope? = null

    protected val scope: CoroutineScope
        get() = _scope ?: error("scope chỉ khả dụng trong onActive/onInactive")

    // ── 3. Public API ─────────────────────────────────────────────────────
    override suspend fun collect(collector: FlowCollector<T>): Nothing = coroutineScope {

        // Bảo vệ toàn bộ transition count/scope bằng mutex — tránh trường hợp
        // collector A giảm về 0 + cancel scope, cùng lúc collector B tăng lên 1
        // rồi bị A ghi đè `_scope = null`.
        transitionMutex.withLock {
            if (++count == 1) {
                val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
                _scope = newScope
                newScope.launch { onActive() }
            }
        }
        try {
            source.collect(collector)
        } finally {
            // NonCancellable để đảm bảo cleanup luôn chạy hết dù outer scope bị cancel.
            withContext(NonCancellable) {
                transitionMutex.withLock {
                    if (--count == 0) {
                        val toCancel = _scope
                        _scope = null
                        onInactive()
                        toCancel?.cancel()
                    }
                }
            }
        }
    }

    // ── 4. Overridable hooks (protected) ──────────────────────────────────
    protected open suspend fun onActive() {}
    protected open suspend fun onInactive() {}
}

fun <T> mutableStateFlow(
    def: T,
    action: suspend MutableStateFlow<T>.() -> Unit
): MutableStateFlow<T> = object : ActiveStateFlow<T>(def) {
    override suspend fun onActive() { action(this) }
}