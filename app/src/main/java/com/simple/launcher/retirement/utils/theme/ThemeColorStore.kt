package com.simple.launcher.retirement.utils.theme

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

object ThemeColorStore {

    private val _colorMapFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    val colorMapFlow: StateFlow<Map<String, Int>> = _colorMapFlow.asStateFlow()

    private val colorByAttrMap = mutableMapOf<Int, Int>()

    /**
     * Load toàn bộ color attr từ theme
     *
     * key   = tên attr
     * value = color int
     */
    fun load(context: Context) {

        val packageName = context.packageName

        val nameMap = mutableMapOf<String, Int>()
        val attrMap = mutableMapOf<Int, Int>()

        try {

            val attrClass = Class.forName("$packageName.R\$attr")

            attrClass.fields.forEach { field ->

                runCatching {

                    val attrId = field.getInt(null)

                    val color = context.getThemeColor(attrId)

                    nameMap[field.name] = color
                    attrMap[attrId] = color
                }
            }

        } catch (_: Exception) {
        }

        colorByAttrMap.clear()
        colorByAttrMap.putAll(attrMap)

        _colorMapFlow.value = nameMap
    }

    /**
     * Lấy color theo tên attr
     *
     * Ví dụ:
     * getColor("colorPrimary")
     */
    @ColorInt
    fun getColor(attrName: String): Int? {

        return _colorMapFlow.value[attrName]
    }

    /**
     * Lấy color theo attr id
     *
     * Ví dụ:
     * getColor(R.attr.colorPrimary)
     */
    @ColorInt
    fun getColor(@AttrRes attrId: Int): Int? {

        return colorByAttrMap[attrId]
    }
}

/**
 * Resolve color từ theme attr
 */
@ColorInt
fun Context.getThemeColor(@AttrRes attrId: Int): Int {

    val typedValue = TypedValue()

    theme.resolveAttribute(
        attrId,
        typedValue,
        true
    )

    return typedValue.data
}

class TestViewModel : BaseViewModel() {

    val primaryColor =
        themes
            .map { map ->

                map["colorPrimary"]
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )
}