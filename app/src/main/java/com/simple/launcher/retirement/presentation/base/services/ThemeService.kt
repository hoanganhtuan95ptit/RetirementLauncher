package com.simple.launcher.retirement.presentation.base.services

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.launcher.retirement.utils.exts.coroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

val colorMapFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
private val idAndNameMap = mutableMapOf<Int, String>()

fun Map<String, Any>.getColor(@AttrRes attrId: Int, @ColorInt defaultColor: Int = android.graphics.Color.BLACK): Int {

    return idAndNameMap[attrId]?.let {

        colorMapFlow.value[it]
    } ?: defaultColor
}

@AutoRegister(apis = [ActivityCreatedService::class])
class ThemeService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        load(fragmentActivity)
    }

    private fun load(context: FragmentActivity) = context.lifecycleScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

        val packageName = context.packageName
        val nameMap = mutableMapOf<String, Int>()
        val idToNameMap = mutableMapOf<Int, String>()

        listOfNotNull(
            android.R.attr::class.java,
            runCatching { Class.forName("com.google.android.material.R\$attr") }.getOrNull(),
            runCatching { Class.forName("$packageName.R\$attr") }.getOrNull()
        ).forEach { clazz ->

            loadFromClass(context, clazz, nameMap, idToNameMap)
        }

        idAndNameMap.clear()
        idAndNameMap.putAll(idToNameMap)

        colorMapFlow.value = nameMap
    }

    private fun loadFromClass(
        context: Context,
        clazz: Class<*>,
        nameMap: MutableMap<String, Int>,
        idMap: MutableMap<Int, String>
    ) = runCatching {

        clazz.fields.forEach { field ->

            processField(context, field, nameMap, idMap)
        }
    }

    private fun processField(
        context: Context,
        field: java.lang.reflect.Field,
        nameMap: MutableMap<String, Int>,
        idMap: MutableMap<Int, String>
    ) = runCatching {

        val attrId = field.getInt(null)
        val color = context.getThemeColorOrNull(attrId) ?: return@runCatching

        nameMap[field.name] = color
        idMap[attrId] = field.name
    }
}

@ColorInt
fun Context.getThemeColorOrNull(@AttrRes attrId: Int): Int? {

    val typedValue = TypedValue()
    val isResolved = theme.resolveAttribute(attrId, typedValue, true)
    if (!isResolved) return null

    val isColor = typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT
    if (!isColor) return null

    return typedValue.data
}

@ColorInt
fun Context.getThemeColor(@AttrRes attrId: Int): Int {

    return getThemeColorOrNull(attrId) ?: android.graphics.Color.TRANSPARENT
}
