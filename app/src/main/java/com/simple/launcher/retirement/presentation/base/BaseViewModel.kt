package com.simple.launcher.retirement.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.presentation.base.services.colorMapFlow
import com.simple.launcher.retirement.presentation.base.services.sizeMapFlow
import com.simple.launcher.retirement.presentation.base.services.stringMapFlow
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.exts.colorBackground
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

open class BaseViewModel : ViewModel() {

    val sizes = sizeMapFlow

    val themes = colorMapFlow

    val strings = stringMapFlow

    val resources: StateFlow<Map<String, Any>> = combineState(
        sizes,
        themes,
        strings,
        emptyMap()
    ) { sizes, themes, strings ->

        val data = hashMapOf<String, Any>()
        data.putAll(sizes)
        data.putAll(themes)
        data.putAll(strings)
        value = data
    }

    val background: StateFlow<Background> = themes.map { themeMap ->
        val backgroundColor = themeMap.colorBackground
        Background.Builder()
            .backgroundColor(backgroundColor)
            .build()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Background.Builder().build())

    open val bottomSheet: StateFlow<BottomSheetState> = themes.map { themeMap ->
        val backgroundColor = themeMap.colorBackground
        val anchorColor = themeMap.textColorSecondary

        buildBottomSheetState(
            backgroundColor = backgroundColor,
            anchorColor = anchorColor,
            showAnchor = true
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BottomSheetState.empty())

    protected var <T> StateFlow<T>.currentValue: T
        get() = value
        set(value) {

            val mutable = this as? MutableStateFlow<*>
                ?: error("not support ${this.javaClass.name}")

            @Suppress("UNCHECKED_CAST")
            (mutable as MutableStateFlow<T>).value = value
        }
}
