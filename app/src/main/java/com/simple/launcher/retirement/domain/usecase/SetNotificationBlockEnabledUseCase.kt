package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.CancellationException

/**
 * Bật/tắt tính năng Notification Block.
 *
 * Cùng contract với [SetAppMonitoringEnabledUseCase]:
 * - Trước khi ghi pref: đảm bảo các quyền cần thiết đã được cấp.
 * - Nếu bất kỳ quyền nào bị từ chối: không đổi pref, trả về false.
 * - Kết thúc (thành công hay không) đều clear pending flag để lần sau
 *   [PreferenceRepository.getPendingNotificationBlockEnabled] không còn trigger apply.
 */
class SetNotificationBlockEnabledUseCase(
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

            preferenceRepository.setNotificationBlockEnabled(isEnabled)
            shouldClearPending = true
            return true
        } catch (cancellationException: CancellationException) {

            throw cancellationException
        } finally {

            if (shouldClearPending) {

                preferenceRepository.setPendingNotificationBlockEnabled(null)
            }
        }
    }

    private suspend fun checkPermissions(isEnabled: Boolean): Boolean {

        // Khi tắt: chỉ cần PIN (giống các flow protect khác) — không cần Notification
        // Access, vì tắt không yêu cầu tương tác với NotificationListenerService.
        if (!isEnabled) {

            return permissionRepository.requirePinPermissions()
        }

        return permissionRepository.requireNotificationListenerAccess()
    }

    companion object {

        val instance: SetNotificationBlockEnabledUseCase by lazy {

            SetNotificationBlockEnabledUseCase(
                PreferenceRepository.instance,
                PermissionRepository.instance
            )
        }
    }
}
