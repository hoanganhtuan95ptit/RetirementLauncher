package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.SOSConfig
import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.CancellationException

class SetEmergencyCallEnabledUseCase(
    private val preferenceRepository: PreferenceRepository,
    private val permissionRepository: PermissionRepository
) {

    suspend operator fun invoke(config: SOSConfig): Boolean {

        var shouldClearPending = false
        try {

            if (!checkPermissions(config)) {

                shouldClearPending = true
                return false
            }

            preferenceRepository.setEmergencyTimeout(config.timeout)
            preferenceRepository.setExclusionPeriods(config.exclusionPeriods)
            preferenceRepository.setEmergencyCallEnabled(config.isEnabled)
            shouldClearPending = true
            return true
        } catch (cancellationException: CancellationException) {

            throw cancellationException
        } finally {

            if (shouldClearPending) {

                preferenceRepository.setPendingEmergencyConfig(null)
            }
        }
    }

    private suspend fun checkPermissions(config: SOSConfig): Boolean {

        if (!config.isEnabled) {

            return permissionRepository.requirePinPermissions()
        }

        return permissionRepository.requireEmergencyContact() &&
                permissionRepository.requireCallPermission() &&
                permissionRepository.requireUserActivityAccessibilityPermission() &&
                permissionRepository.requireDefaultLauncher()
    }

    companion object {

        val instance: SetEmergencyCallEnabledUseCase by lazy {

            SetEmergencyCallEnabledUseCase(
                PreferenceRepository.instance,
                PermissionRepository.instance
            )
        }
    }
}
