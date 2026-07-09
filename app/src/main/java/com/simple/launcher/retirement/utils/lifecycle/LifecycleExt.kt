package com.simple.launcher.retirement.utils.lifecycle

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T> Flow<T>.observe(lifecycleOwner: LifecycleOwner, action: suspend (T) -> Unit) {

    lifecycleOwner.lifecycleScope.launch {

        collectLatest(action)
    }
}

fun <T> Flow<T>.observe(fragment: Fragment, action: suspend (T) -> Unit) {

    fragment.viewLifecycleOwner.lifecycleScope.launch {

        collectLatest(action)
    }
}
