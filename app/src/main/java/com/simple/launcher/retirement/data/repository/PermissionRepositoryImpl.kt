package com.simple.launcher.retirement.data.repository

import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.utils.permission.PermissionManager

class PermissionRepositoryImpl : PermissionRepository {

    override fun hasFilePermission(): Boolean = PermissionManager.hasFilePermission()

    override fun hasUsageStatsPermission(): Boolean = PermissionManager.hasUsageStatsPermission()

    override fun hasOverlayPermission(): Boolean = PermissionManager.hasOverlayPermission()

    override fun hasCallBlockPermissions(): Boolean = PermissionManager.hasCallBlockPermissions()

    override fun hasCallScreeningRole(): Boolean = PermissionManager.hasCallScreeningRole()

    override fun getCallBlockPermissions(): Array<String> = PermissionManager.getCallBlockPermissions()

    override fun isDefaultLauncher(): Boolean = PermissionManager.isDefaultLauncher()

    override fun hasContactPermission(): Boolean = PermissionManager.hasContactPermission()

    override fun hasPinPermission(): Boolean = PermissionManager.hasPinPermission()

    override fun hasCallPermission(): Boolean = PermissionManager.hasCallPermission()

    override fun hasUserActivityAccessibilityPermission(): Boolean = PermissionManager.hasUserActivityAccessibilityPermission()

    override suspend fun requireUsageStatsPermission(): Boolean = PermissionManager.requireUsageStatsPermission()

    override suspend fun requireFilePermission(): Boolean = PermissionManager.requireFilePermission()

    override suspend fun requireOverlayPermission(): Boolean = PermissionManager.requireOverlayPermission()

    override suspend fun requireDefaultLauncher(): Boolean = PermissionManager.requireDefaultLauncher()

    override suspend fun requireCallPermission(): Boolean = PermissionManager.requireCallPermission()

    override suspend fun requireUserActivityAccessibilityPermission(): Boolean =
        PermissionManager.requireUserActivityAccessibilityPermission()

    override suspend fun requireEmergencyContact(): Boolean = PermissionManager.requireEmergencyContact()

    override suspend fun requireCallBlockPermissions(): Boolean = PermissionManager.requireCallBlockPermissions()

    override suspend fun requirePinPermissions(): Boolean = PermissionManager.requirePinPermissions()

    override suspend fun requireAppMonitoringIntro(): Boolean = PermissionManager.requireAppMonitoringIntro()

    override suspend fun requireEmergencyCallIntro(): Boolean = PermissionManager.requireEmergencyCallIntro()

    override suspend fun requireFileCleanupIntro(): Boolean = PermissionManager.requireFileCleanupIntro()

    override suspend fun requireCallBlockIntro(): Boolean = PermissionManager.requireCallBlockIntro()
}
