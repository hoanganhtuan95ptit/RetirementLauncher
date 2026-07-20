package com.simple.launcher.retirement.domain.usecase

import android.util.Log
import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.CancellationException

class SetAppMonitoringEnabledUseCase(
    private val preferenceRepository: PreferenceRepository,
    private val permissionRepository: PermissionRepository
) {

    suspend operator fun invoke(isEnabled: Boolean): Boolean {

        var shouldClearPending = false
        try {

            if (!checkPermissions(isEnabled)) {

                shouldClearPending = true
                return false
            }

            Log.d("tuanha", "invoke: setAppBlockEnabled isEnabled:$isEnabled")
            preferenceRepository.setAppBlockEnabled(isEnabled)
            shouldClearPending = true
            return true
        } catch (cancellationException: CancellationException) {

            throw cancellationException
        } finally {

            Log.d("tuanha", "invoke: ")
            if (shouldClearPending) {

                preferenceRepository.setPendingAppBlockEnabled(null)
            }
        }
    }

    private suspend fun checkPermissions(isEnabled: Boolean): Boolean {

        if (!isEnabled) {

            return permissionRepository.requirePinPermissions()
        }

        return permissionRepository.requireAppMonitoringIntro() &&
                permissionRepository.requireAppList() &&
                permissionRepository.requireOverlayPermission() &&
                permissionRepository.requireUsageStatsPermission() &&
                permissionRepository.requireDefaultLauncher()
    }

    companion object {

        val instance: SetAppMonitoringEnabledUseCase by lazy {

            SetAppMonitoringEnabledUseCase(
                PreferenceRepository.instance,
                PermissionRepository.instance
            )
        }
    }
}
