package com.simple.launcher.retirement.presentation.base

import androidx.lifecycle.ViewModel
import com.simple.launcher.retirement.presentation.base.services.colorMapFlow
import com.simple.launcher.retirement.presentation.base.services.sizeMapFlow
import com.simple.launcher.retirement.presentation.base.services.stringMapFlow
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.exts.colorBackground
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    val background: StateFlow<Background?> = combineState(themes, null) { themes ->

        val backgroundColor = themes.colorBackground
        value = Background.Builder()
            .backgroundColor(backgroundColor)
            .build()
    }

    open val bottomSheet: StateFlow<BottomSheetState?> = combineState(themes, null) { themes ->

        val backgroundColor = themes.colorBackground
        val anchorColor = themes.textColorSecondary

        value = buildBottomSheetState(
            backgroundColor = backgroundColor,
            anchorColor = anchorColor,
            showAnchor = true
        )
    }

    protected var <T> StateFlow<T>.currentValue: T
        get() = value
        set(value) {

            val mutable = this as? MutableStateFlow<*>
                ?: error("not support ${this.javaClass.name}")

            @Suppress("UNCHECKED_CAST")
            (mutable as MutableStateFlow<T>).value = value
        }
}
