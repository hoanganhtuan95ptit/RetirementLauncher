package com.simple.launcher.retirement.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun <T1, R> ViewModel.combineState(
    flow1: Flow<T1>,
    initialValue: R,
    transform: suspend (T1) -> R
): StateFlow<R> = flow1.map {
    transform.invoke(it)
}.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)

fun <T1, T2, R> ViewModel.combineState(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    initialValue: R,
    transform: suspend (T1, T2) -> R
): StateFlow<R> = combine(flow1, flow2, transform)
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)

fun <T1, T2, T3, R> ViewModel.combineState(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    initialValue: R,
    transform: suspend (T1, T2, T3) -> R
): StateFlow<R> = combine(flow1, flow2, flow3, transform)
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)

fun <T1, T2, T3, T4, R> ViewModel.combineState(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    initialValue: R,
    transform: suspend (T1, T2, T3, T4) -> R
): StateFlow<R> = combine(flow1, flow2, flow3, flow4, transform)
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)

fun <T1, T2, T3, T4, T5, R> ViewModel.combineState(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    initialValue: R,
    transform: suspend (T1, T2, T3, T4, T5) -> R
): StateFlow<R> = combine(flow1, flow2, flow3, flow4, flow5, transform)
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)

