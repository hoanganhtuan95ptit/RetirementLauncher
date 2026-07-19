package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.CancellationException

class SetCallBlockEnabledUseCase(
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

            preferenceRepository.setCallBlockEnabled(isEnabled)
            shouldClearPending = true
            return true
        } catch (cancellationException: CancellationException) {

            throw cancellationException
        } finally {

            if (shouldClearPending) {

                preferenceRepository.setPendingCallBlockEnabled(null)
            }
        }
    }

    private suspend fun checkPermissions(isEnabled: Boolean): Boolean {

        if (!isEnabled) {

            return permissionRepository.requirePinPermissions()
        }

        return permissionRepository.requireCallBlockIntro() &&
                permissionRepository.requireCallBlockPermissions()
    }

    companion object {

        val instance: SetCallBlockEnabledUseCase by lazy {

            SetCallBlockEnabledUseCase(
                PreferenceRepository.instance,
                PermissionRepository.instance
            )
        }
    }
}
