package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.CancellationException

class SetFileCleanupEnabledUseCase(
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

            preferenceRepository.setFileCleanupEnabled(isEnabled)
            shouldClearPending = true
            return true
        } catch (cancellationException: CancellationException) {

            throw cancellationException
        } finally {

            if (shouldClearPending) {

                preferenceRepository.setPendingFileCleanupEnabled(null)
            }
        }
    }

    private suspend fun checkPermissions(isEnabled: Boolean): Boolean {

        if (!isEnabled) {

            return permissionRepository.requirePinPermissions()
        }

        return permissionRepository.requireFileCleanupIntro() &&
                permissionRepository.requireFilePermission()
    }

    companion object {

        val instance: SetFileCleanupEnabledUseCase by lazy {

            SetFileCleanupEnabledUseCase(
                PreferenceRepository.instance,
                PermissionRepository.instance
            )
        }
    }
}
