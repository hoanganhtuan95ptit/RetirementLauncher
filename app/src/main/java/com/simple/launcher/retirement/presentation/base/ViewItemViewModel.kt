package com.simple.launcher.retirement.presentation.base

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.utils.JobQueue
import com.simple.launcher.retirement.utils.combineState
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

    val jobQueue by lazy { JobQueue() }

    val viewItemMap = MutableStateFlow<MutableMap<Double, List<ViewItem>>>(mutableMapOf())

    val viewItemList: StateFlow<List<ViewItem>> = combineState(
        viewItemMap,
        emptyList()
    ) { viewItemMap ->

        viewItemMap.toList()
            .sortedBy { it.first }
            .flatMap { it.second }
    }


    fun updateItem(groupViewItem: GroupViewItem) {
        updateItem(groupViewItem.order, groupViewItem.list)
    }

    fun updateItem(order: Int, list: List<ViewItem>?) {
        updateItem(order.toDouble(), list)
    }

    fun updateItem(order: Double, list: List<ViewItem>?) = jobQueue.submit {
        if (list != null) viewItemMap.value = viewItemMap.value.toMutableMap().apply {
            put(order, list)
        }
    }
}