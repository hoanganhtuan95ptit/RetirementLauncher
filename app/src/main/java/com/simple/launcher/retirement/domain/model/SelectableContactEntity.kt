package com.simple.launcher.retirement.domain.model

import com.simple.adapter.ViewItem

data class SelectableContactEntity(
    val contact: ContactEntity,
    val isSelected: Boolean
) : ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(contact.id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        isSelected to "isSelected"
    )
}
