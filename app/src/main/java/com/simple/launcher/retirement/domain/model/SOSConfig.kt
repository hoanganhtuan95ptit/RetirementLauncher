package com.simple.launcher.retirement.domain.model

data class SOSConfig(
    val isEnabled: Boolean,
    val timeout: Long,
    val exclusionPeriods: List<ExclusionPeriod>
)
