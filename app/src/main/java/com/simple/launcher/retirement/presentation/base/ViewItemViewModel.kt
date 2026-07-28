package com.simple.launcher.retirement.presentation.base

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.utils.JobQueue
import com.simple.launcher.retirement.utils.exts.combineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun GroupViewItem(
    order: Int,
    list: List<ViewItem>? = null
) = GroupViewItem(
    order.toDouble(),
    list
)

data class GroupViewItem(
    val order: Double = 0.0,
    val list: List<ViewItem>? = null
)

abstract class ViewItemViewModel : BaseViewModel() {

    private val jobQueue by lazy { JobQueue() }

    // Mỗi service sở hữu một slot riêng và thay toàn bộ list cho slot đó.
    private val viewItemMap = MutableStateFlow<Map<Double, List<ViewItem>>>(emptyMap())

    val viewItemList: StateFlow<List<ViewItem>> = combineState(
        flow1 = viewItemMap,
        initialValue = emptyList()
    ) { itemMap ->

        val values = itemMap.toList()
            .sortedBy { it.first }
            .flatMap { it.second }
            .toMutableList()

        value = wrapViewItem(values)
    }

    fun updateItem(groupViewItem: GroupViewItem?) {

        updateItem(groupViewItem?.order ?: return, groupViewItem.list)
    }

    fun updateItem(order: Int, list: List<ViewItem>?) {

        updateItem(order.toDouble(), list)
    }

    fun updateItem(order: Double, list: List<ViewItem>?) = jobQueue.submit {

        viewItemMap.value = viewItemMap.value.toMutableMap().apply {
            if (list.isNullOrEmpty()) {

                remove(order)
                return@apply
            }

            put(order, list)
        }
    }

    open protected suspend fun wrapViewItem(values: MutableList<ViewItem>): List<ViewItem> {

        return values
    }
}
