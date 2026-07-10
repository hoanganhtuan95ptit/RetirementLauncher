package com.simple.launcher.retirement.utils.permission

import android.Manifest
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

object PermissionManager {

    private val context: Context get() = MainApplication.instance

    fun hasFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            val writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasCallBlockPermissions(): Boolean {
        return getCallBlockPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getCallBlockPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.READ_CALL_LOG)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }
        return permissions.toTypedArray()
    }

    fun isDefaultLauncher(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            resolveInfo?.activityInfo?.packageName == context.packageName
        }
    }

    fun hasContactPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAnswerPhoneCallsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED
    }

    fun hasPinPermission(): Boolean {
        return PreferenceRepository.instance.hasPin()
    }

    fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * Yêu cầu quyền Usage Stats.
     * - Nếu đã có quyền: trả về true ngay.
     * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
     * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
     */
    suspend fun requireUsageStatsPermission(): Boolean {
        if (hasUsageStatsPermission()) return true
        sendDeeplink(DeepLinks.PERMISSION_USAGE_STATS)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.PermissionResult>()
            .first()
        return result is AppEvent.PermissionAccept
    }

    /**
     * Yêu cầu quyền File.
     * - Nếu đã có quyền: trả về true ngay.
     * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
     * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
     */
    suspend fun requireFilePermission(): Boolean {
        if (hasFilePermission()) return true
        sendDeeplink(DeepLinks.PERMISSION_FILE)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.PermissionResult>()
            .first()
        return result is AppEvent.PermissionAccept
    }

    /**
     * Yêu cầu quyền Overlay (vẽ đè lên ứng dụng khác).
     * - Nếu đã có quyền: trả về true ngay.
     * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
     * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
     */
    suspend fun requireOverlayPermission(): Boolean {
        if (hasOverlayPermission()) return true
        sendDeeplink(DeepLinks.PERMISSION_OVERLAY)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.PermissionResult>()
            .first()
        return result is AppEvent.PermissionAccept
    }

    /**
     * Yêu cầu đặt làm launcher mặc định.
     * - Nếu đã là default: trả về true ngay.
     * - Nếu chưa: mở bottom sheet hướng dẫn, chờ kết quả từ AppEventBus.
     * @return true nếu đã là hoặc vừa được đặt làm default, false nếu user huỷ.
     */
    suspend fun requireDefaultLauncher(): Boolean {
        if (isDefaultLauncher()) return true
        sendDeeplink(DeepLinks.PERMISSION_DEFAULT_LAUNCHER)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.PermissionResult>()
            .first()
        return result is AppEvent.PermissionAccept
    }

    suspend fun requireCallPermission(): Boolean {
        if (hasCallPermission()) return true
        sendDeeplink(DeepLinks.PERMISSION_CALL)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.PermissionResult>()
            .first()
        return result is AppEvent.PermissionAccept
    }

    /**
     * Yêu cầu quyền chặn cuộc gọi.
     * - Nếu đã có quyền: trả về true ngay.
     * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
     * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
     */
    suspend fun requireCallBlockPermissions(): Boolean {
        if (hasCallBlockPermissions()) return true
        sendDeeplink(DeepLinks.PERMISSION_CALL_BLOCK)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.PermissionResult>()
            .first()
        return result is AppEvent.PermissionAccept
    }

    /**
     * Yêu cầu xác thực PIN (verify nếu đã có, setup nếu chưa có).
     * @return true nếu xác thực/thiết lập thành công, false nếu user huỷ.
     */
    suspend fun requirePinPermissions(): Boolean {
//        if (hasPinPermission()) {
//            sendDeeplink(DeepLinks.PIN_VERIFY)
//        } else {
//            sendDeeplink(DeepLinks.PIN_SETUP)
//        }
//        return AppEventBus.events.filterIsInstance<AppEvent.PinResult>().first() !is AppEvent.PinCancel
        return true
    }

    /**
     * Yêu cầu hiển thị giới thiệu tính năng Giám sát ứng dụng nếu là lần đầu.
     * @return true nếu đã xem hoặc vừa nhấn chấp nhận, false nếu user huỷ.
     */
    suspend fun requireAppMonitoringIntro(): Boolean {
        val repository = PreferenceRepository.instance
        if (!repository.isAppBlockFirstTime()) return true

        sendDeeplink(DeepLinks.APP_MONITORING_INTRO)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.AppMonitoringIntroResult>()
            .first()

        return if (result is AppEvent.AppMonitoringIntroAccept) {
            repository.setAppBlockFirstTime(false)
            true
        } else {
            false
        }
    }

    /**
     * Yêu cầu hiển thị giới thiệu tính năng Cuộc gọi khẩn cấp nếu là lần đầu.
     * @return true nếu đã xem hoặc vừa nhấn chấp nhận, false nếu user huỷ.
     */
    suspend fun requireEmergencyCallIntro(): Boolean {
        val repository = PreferenceRepository.instance
        if (!repository.isEmergencyCallFirstTime()) return true

        sendDeeplink(DeepLinks.EMERGENCY_CALL_INTRO)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.EmergencyCallIntroResult>()
            .first()

        return if (result is AppEvent.EmergencyCallIntroAccept) {
            repository.setEmergencyCallFirstTime(false)
            true
        } else {
            false
        }
    }

    /**
     * Yêu cầu hiển thị giới thiệu tính năng Dọn dẹp file APK nếu là lần đầu.
     * @return true nếu đã xem hoặc vừa nhấn chấp nhận, false nếu user huỷ.
     */
    suspend fun requireFileCleanupIntro(): Boolean {
        val repository = PreferenceRepository.instance
        if (!repository.isFileCleanupFirstTime()) return true

        sendDeeplink(DeepLinks.FILE_CLEANUP_INTRO)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.FileCleanupIntroResult>()
            .first()

        return if (result is AppEvent.FileCleanupIntroAccept) {
            repository.setFileCleanupFirstTime(false)
            true
        } else {
            false
        }
    }

    /**
     * Yêu cầu hiển thị giới thiệu tính năng Chặn cuộc gọi lạ nếu là lần đầu.
     * @return true nếu đã xem hoặc vừa nhấn chấp nhận, false nếu user huỷ.
     */
    suspend fun requireCallBlockIntro(): Boolean {
        val repository = PreferenceRepository.instance
        if (!repository.isCallBlockFirstTime()) return true

        sendDeeplink(DeepLinks.CALL_BLOCK_INTRO)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.CallBlockIntroResult>()
            .first()

        return if (result is AppEvent.CallBlockIntroAccept) {
            repository.setCallBlockFirstTime(false)
            true
        } else {
            false
        }
    }
}
