package com.simple.launcher.retirement.domain.model

import com.simple.adapter.ViewItem

data class SelectableAppEntity(
    val app: AppEntity,
    var isSelected: Boolean
) : ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(app.packageName)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        isSelected to "isSelected"
    )
}
