package com.simple.launcher.retirement.presentation.settings

import android.content.Context
import android.util.Log
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.pin_setup.Pin
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Yêu cầu xác thực PIN (verify nếu đã có, setup nếu chưa có).
 * @return true nếu xác thực/thiết lập thành công, false nếu user huỷ.
 */
suspend fun requirePin(): Boolean {
    val result = Pin.verify()
    Log.d("tuanha", "requirePin: $result")
    return result !is Pin.PinCancel
}

/**
 * Yêu cầu quyền Usage Stats.
 * - Nếu đã có quyền: trả về true ngay.
 * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
 * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
 */
suspend fun requireUsageStatsPermission(context: Context): Boolean {
    if (PermissionManager.hasUsageStatsPermission(context)) return true
    Log.d("tuanha", "requireUsageStatsPermission: ")
    sendDeeplink(DeepLinks.PERMISSION_USAGE_STATS)
    val result = AppEventBus.events
        .map {
            Log.d("tuanha", "requireUsageStatsPermission: ${it.javaClass.name}")
            it
        }
        .filterIsInstance<AppEvent.PermissionResult>()
        .first()
    return result is AppEvent.PermissionAccept
}
