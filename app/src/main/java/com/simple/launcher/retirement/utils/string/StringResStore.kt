package com.simple.launcher.retirement.utils.string

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

object StringResStore {

    private val _stringMapFlow = MutableStateFlow<Map<String, String>>(emptyMap())
    val stringMapFlow: StateFlow<Map<String, String>> = _stringMapFlow.asStateFlow()

    private val stringByIdMap = mutableMapOf<Int, String>()

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
                    idMap[resId] = value
                }
            }

        } catch (_: Exception) {
        }

        stringByIdMap.clear()
        stringByIdMap.putAll(idMap)

        _stringMapFlow.value = nameMap
    }

    /**
     * getString("app_name")
     */
    fun getString(resName: String): String? {

        return _stringMapFlow.value[resName]
    }

    /**
     * getString(R.string.app_name)
     */
    fun getString(@StringRes resId: Int): String? {

        return stringByIdMap[resId]
    }
}

/**
 * Skill: Lấy giá trị string trực tiếp từ Resource ID
 */
fun Int.asStringRes(): String = StringResStore.getString(this).orEmpty()

/**
 * Skill: Lấy giá trị string trực tiếp từ tên Resource
 */
fun String.asStringRes(): String = StringResStore.getString(this).orEmpty()

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

class Test : BaseViewModel() {


    /**
     * Flow test sử dụng các Skill mới
     */
    val testName: StateFlow<String> =
        "app_name".observeStringRes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = R.string.app_name.asStringRes()
            )

    /**
     * Ví dụ sử dụng format
     */
    fun getWelcome(name: String): String {
        return "welcome_msg".asFormattedStringRes(name)
    }
}
