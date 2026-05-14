package com.simple.launcher.retirement.domain.model

import com.simple.adapter.ViewItem

data class SelectableContactEntity(
    val contact: ContactEntity,
    var isSelected: Boolean
) : ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(contact.phoneNumber)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        isSelected to "isSelected"
    )
}
