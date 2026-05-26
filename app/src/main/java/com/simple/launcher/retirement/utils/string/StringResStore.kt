package com.simple.launcher.retirement.utils.string

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

object StringResStore {

    private val _stringMapFlow = MutableStateFlow<Map<String, String>>(emptyMap())
    val stringMapFlow: StateFlow<Map<String, String>> = _stringMapFlow.asStateFlow()

    val idAndNameMap = mutableMapOf<Int, String>()

    fun load(context: Context) {
        val resources = context.resources
        val packageName = context.packageName

        val nameMap = mutableMapOf<String, String>()
        val idMap = mutableMapOf<Int, String>()

        try {
            val stringClass = Class.forName("$packageName.R\$string")
            stringClass.fields.forEach { field ->
                runCatching {
                    val resId = field.getInt(null)
                    val value = resources.getString(resId)

                    nameMap[field.name] = value
                    idMap[resId] = field.name
                }
            }
        } catch (_: Exception) {
        }

        idAndNameMap.clear()
        idAndNameMap.putAll(idMap)

        _stringMapFlow.value = nameMap
    }
}

/**
 * Extension giúp lấy string từ stringsMap thông qua Resource ID
 */
fun Map<String, String>.getString(resId: Int): String {
    return StringResStore.idAndNameMap[resId]?.let {
        StringResStore.stringMapFlow.value[it]
    } ?: return ""
}

/**
 * Skill: Lấy giá trị string trực tiếp từ Resource ID (Dựa trên giá trị hiện tại)
 */
fun Int.asStringRes(): String = StringResStore.stringMapFlow.value.getString(this)

/**
 * Skill: Lấy giá trị string trực tiếp từ tên Resource
 */
fun String.asStringRes(): String = StringResStore.stringMapFlow.value[this].orEmpty()

/**
 * Skill: Hỗ trợ format string với tham số (ví dụ: "Chào %s")
 */
fun String.asFormattedStringRes(vararg args: Any): String {
    val raw = this.asStringRes()
    return if (raw.isEmpty()) "" else String.format(raw, *args)
}

/**
 * Skill: Observe một string cụ thể dưới dạng Flow
 */
fun String.observeStringRes(): Flow<String> =
    StringResStore.stringMapFlow.map { it[this].orEmpty() }
