package com.simple.launcher.retirement.presentation.settings

import android.content.Context
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

/**
 * Yêu cầu xác thực PIN (verify nếu đã có, setup nếu chưa có).
 * @return true nếu xác thực/thiết lập thành công, false nếu user huỷ.
 */
suspend fun requirePin(): Boolean {

    if (PreferenceRepository.instance.hasPin()) {

        sendDeeplink(DeepLinks.PIN_VERIFY)
    } else {

        sendDeeplink(DeepLinks.PIN_SETUP)
    }

    return AppEventBus.events.filterIsInstance<AppEvent.PinResult>().first() !is AppEvent.PinCancel
}

/**
 * Yêu cầu quyền Usage Stats.
 * - Nếu đã có quyền: trả về true ngay.
 * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
 * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
 */
suspend fun requireUsageStatsPermission(context: Context): Boolean {
    if (PermissionManager.hasUsageStatsPermission(context)) return true
    sendDeeplink(DeepLinks.PERMISSION_USAGE_STATS)
    val result = AppEventBus.events
        .filterIsInstance<AppEvent.PermissionResult>()
        .first()
    return result is AppEvent.PermissionAccept
}
