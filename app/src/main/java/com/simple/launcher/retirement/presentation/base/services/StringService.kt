package com.simple.launcher.retirement.presentation.base.services

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.launcher.retirement.utils.exts.coroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val stringMapFlow = MutableStateFlow<Map<String, String>>(emptyMap())
private val idAndNameMap = mutableMapOf<Int, String>()

fun Map<String, String>.getString(resId: Int): String {

    return idAndNameMap[resId]?.let {

        stringMapFlow.value[it]
    } ?: return ""
}

@AutoRegister(apis = [ActivityCreatedService::class])
class StringService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        load(fragmentActivity)
    }

    private fun load(context: FragmentActivity) = context.lifecycleScope.launch(coroutineExceptionHandler + Dispatchers.Default) {

        val resources = context.resources
        val packageName = context.packageName

        val nameMap = mutableMapOf<String, String>()
        val idMap = mutableMapOf<Int, String>()

        runCatching {

            val stringClass = Class.forName("$packageName.R\$string")
            stringClass.fields.forEach { field ->

                runCatching {

                    val resId = field.getInt(null)
                    val value = resources.getString(resId)

                    nameMap[field.name] = value
                    idMap[resId] = field.name
                }
            }
        }

        idAndNameMap.clear()
        idAndNameMap.putAll(idMap)

        stringMapFlow.value = nameMap
    }
}

fun Int.asStringRes(): String = stringMapFlow.value.getString(this)

fun String.asStringRes(): String = stringMapFlow.value[this].orEmpty()

fun String.asFormattedStringRes(vararg args: Any): String {

    val raw = this.asStringRes()
    return if (raw.isEmpty()) "" else String.format(raw, *args)
}

fun String.observeStringRes(): Flow<String> =
    stringMapFlow.map { it[this].orEmpty() }
