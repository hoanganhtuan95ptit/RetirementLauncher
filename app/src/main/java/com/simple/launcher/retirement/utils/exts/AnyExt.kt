@file:Suppress("UNCHECKED_CAST")

package com.simple.launcher.retirement.utils.exts

import com.simple.launcher.retirement.presentation.base.services.getString
import com.simple.launcher.retirement.presentation.base.services.stringMapFlow

inline fun <reified T> Any?.asObject(): T {

    return this as T
}

inline fun <reified T> Any?.asObjectOrNull(): T? {

    return this as? T
}


fun Int?.orZero() = this ?: 0


fun Map<String, Any>.getString(resId: Int): String {

    return stringMapFlow.value.getString(resId)
}
