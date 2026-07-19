package com.simple.launcher.retirement.utils.permission

import com.simple.launcher.retirement.domain.repository.PermissionRepository

object PermissionManager {

    private val permissionRepository: PermissionRepository
        get() = PermissionRepository.instance

    fun hasFilePermission(): Boolean = permissionRepository.hasFilePermission()

    fun hasUsageStatsPermission(): Boolean = permissionRepository.hasUsageStatsPermission()

    fun hasOverlayPermission(): Boolean = permissionRepository.hasOverlayPermission()

    fun hasCallBlockPermissions(): Boolean = permissionRepository.hasCallBlockPermissions()

    fun hasCallScreeningRole(): Boolean = permissionRepository.hasCallScreeningRole()

    fun getCallBlockPermissions(): Array<String> = permissionRepository.getCallBlockPermissions()

    fun isDefaultLauncher(): Boolean = permissionRepository.isDefaultLauncher()

    fun hasContactPermission(): Boolean = permissionRepository.hasContactPermission()

    fun hasPinPermission(): Boolean = permissionRepository.hasPinPermission()

    fun hasCallPermission(): Boolean = permissionRepository.hasCallPermission()

    fun hasUserActivityAccessibilityPermission(): Boolean =
        permissionRepository.hasUserActivityAccessibilityPermission()

    suspend fun requireUsageStatsPermission(): Boolean =
        permissionRepository.requireUsageStatsPermission()

    suspend fun requireFilePermission(): Boolean = permissionRepository.requireFilePermission()

    suspend fun requireOverlayPermission(): Boolean = permissionRepository.requireOverlayPermission()

    suspend fun requireDefaultLauncher(): Boolean = permissionRepository.requireDefaultLauncher()

    suspend fun requireCallPermission(): Boolean = permissionRepository.requireCallPermission()

    suspend fun requireUserActivityAccessibilityPermission(): Boolean =
        permissionRepository.requireUserActivityAccessibilityPermission()

    suspend fun requireEmergencyContact(): Boolean = permissionRepository.requireEmergencyContact()

    suspend fun requireAppList(): Boolean = permissionRepository.requireAppList()

    suspend fun requireCallBlockPermissions(): Boolean =
        permissionRepository.requireCallBlockPermissions()

    suspend fun requirePinPermissions(): Boolean = permissionRepository.requirePinPermissions()

    suspend fun requireAppMonitoringIntro(): Boolean =
        permissionRepository.requireAppMonitoringIntro()

    suspend fun requireEmergencyCallIntro(): Boolean =
        permissionRepository.requireEmergencyCallIntro()

    suspend fun requireFileCleanupIntro(): Boolean =
        permissionRepository.requireFileCleanupIntro()

    suspend fun requireCallBlockIntro(): Boolean = permissionRepository.requireCallBlockIntro()
}
