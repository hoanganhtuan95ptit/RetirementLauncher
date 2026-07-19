package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.data.repository.PermissionRepositoryImpl

interface PermissionRepository {

    fun hasFilePermission(): Boolean
    fun hasUsageStatsPermission(): Boolean
    fun hasOverlayPermission(): Boolean
    fun hasCallBlockPermissions(): Boolean
    fun hasCallScreeningRole(): Boolean
    fun getCallBlockPermissions(): Array<String>
    fun isDefaultLauncher(): Boolean
    fun hasContactPermission(): Boolean
    fun hasPinPermission(): Boolean
    fun hasCallPermission(): Boolean
    fun hasUserActivityAccessibilityPermission(): Boolean

    suspend fun requireUsageStatsPermission(): Boolean
    suspend fun requireFilePermission(): Boolean
    suspend fun requireOverlayPermission(): Boolean
    suspend fun requireDefaultLauncher(): Boolean
    suspend fun requireCallPermission(): Boolean
    suspend fun requireUserActivityAccessibilityPermission(): Boolean
    suspend fun requireEmergencyContact(): Boolean
    suspend fun requireCallBlockPermissions(): Boolean
    suspend fun requirePinPermissions(): Boolean
    suspend fun requireAppMonitoringIntro(): Boolean
    suspend fun requireEmergencyCallIntro(): Boolean
    suspend fun requireFileCleanupIntro(): Boolean
    suspend fun requireCallBlockIntro(): Boolean

    companion object {

        val instance: PermissionRepository by lazy { PermissionRepositoryImpl() }
    }
}
