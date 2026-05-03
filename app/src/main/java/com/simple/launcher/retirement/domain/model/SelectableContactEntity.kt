package com.simple.launcher.retirement.domain.model

data class SelectableContactEntity(
    val contact: ContactEntity,
    var isSelected: Boolean
)
