package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.PermissionRepositoryImpl

interface PermissionRepository {

    fun hasUsageStatsPermission(): Boolean
    suspend fun requireUsageStatsPermission(): Boolean

    fun hasOverlayPermission(): Boolean
    suspend fun requireOverlayPermission(): Boolean

    fun isDefaultLauncher(): Boolean
    suspend fun requireDefaultLauncher(): Boolean

    fun hasCallPermission(): Boolean
    suspend fun requireCallPermission(): Boolean

    fun hasUserActivityAccessibilityPermission(): Boolean
    suspend fun requireUserActivityAccessibilityPermission(): Boolean

    fun hasCallBlockPermissions(): Boolean

    fun hasCallScreeningRole(): Boolean

    fun getCallBlockPermissions(): Array<String>
    suspend fun requireCallBlockPermissions(): Boolean

    suspend fun requireCallBlockIntro(): Boolean

    fun hasContactPermission(): Boolean

    suspend fun requireEmergencyContact(): Boolean

    suspend fun requireAppList(): Boolean

    fun hasPinPermission(): Boolean

    suspend fun requirePinPermissions(): Boolean

    suspend fun requireAppMonitoringIntro(): Boolean

    suspend fun requireEmergencyCallIntro(): Boolean

    companion object {

        val instance: PermissionRepository by lazy {
            PermissionRepositoryImpl(MainApplication.instance)
        }
    }
}
