package com.simple.launcher.retirement.utils.exts

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Wrap Android OnBackPressedCallback (Java API) thành Flow theo Rule 3.3.
 *
 * Usage:
 * ```
 * lifecycleScope.launch {
 *
 *     onBackPressedDispatcher.backPressedFlow(this@Activity).collect { handleBack() }
 * }
 * ```
 */
fun OnBackPressedDispatcher.backPressedFlow(owner: LifecycleOwner): Flow<Unit> = callbackFlow {

    val callback = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() { trySend(Unit) }
    }
    addCallback(owner, callback)
    awaitClose { callback.remove() }
}
