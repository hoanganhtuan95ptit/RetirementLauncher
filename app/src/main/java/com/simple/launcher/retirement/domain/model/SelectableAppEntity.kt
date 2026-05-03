package com.simple.launcher.retirement.domain.model

data class SelectableAppEntity(
    val app: AppEntity,
    var isSelected: Boolean
)
