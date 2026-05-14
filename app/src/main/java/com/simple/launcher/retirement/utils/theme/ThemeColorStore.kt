package com.simple.launcher.retirement.utils.theme

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeColorStore {

    private val _colorMapFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    val colorMapFlow: StateFlow<Map<String, Int>> = _colorMapFlow.asStateFlow()

    val attrIdMap = mutableMapOf<Int, Int>()

    /**
     * Load toàn bộ color attr từ theme
     */
    fun load(context: Context) {
        val packageName = context.packageName
        val nameMap = mutableMapOf<String, Int>()
        val idToNameMap = mutableMapOf<Int, Int>()

        // 1. Load System Attributes (Luôn ưu tiên)
        loadFromClass(context, android.R.attr::class.java, nameMap, idToNameMap)

        // 2. Load Material Attributes (Nếu có)
        try {
            val materialAttrClass = Class.forName("com.google.android.material.R\$attr")
            loadFromClass(context, materialAttrClass, nameMap, idToNameMap)
        } catch (_: Exception) {
        }

        // 3. Load App Attributes
        try {
            val appAttrClass = Class.forName("$packageName.R\$attr")
            loadFromClass(context, appAttrClass, nameMap, idToNameMap)
        } catch (_: Exception) {
        }

        attrIdMap.clear()
        attrIdMap.putAll(idToNameMap)

        _colorMapFlow.value = nameMap
    }

    private fun loadFromClass(
        context: Context,
        clazz: Class<*>,
        nameMap: MutableMap<String, Int>,
        idMap: MutableMap<Int, Int>
    ) {
        clazz.fields.forEach { field ->
            runCatching {
                val attrId = field.getInt(null)
                val color = context.getThemeColorOrNull(attrId)
                if (color != null) {
                    nameMap[field.name] = color
                    idMap[attrId] = color
                }
            }
        }
    }
}

/**
 * Extension giúp lấy màu từ themeMap thông qua R.attr ID
 */
fun Map<String, Int>.getColor(@AttrRes attrId: Int): Int? {
    return ThemeColorStore.attrIdMap[attrId]
}

/**
 * Resolve color từ theme attr, trả về null nếu không phải color hoặc không tồn tại
 */
@ColorInt
fun Context.getThemeColorOrNull(@AttrRes attrId: Int): Int? {
    val typedValue = TypedValue()
    if (theme.resolveAttribute(attrId, typedValue, true)) {
        // Kiểm tra xem attribute có phải là màu sắc không
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return typedValue.data
        }
    }
    return null
}

@ColorInt
fun Context.getThemeColor(@AttrRes attrId: Int): Int {
    return getThemeColorOrNull(attrId) ?: android.graphics.Color.TRANSPARENT
}
