package com.simple.launcher.retirement.utils.permission

import android.Manifest
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.emergency.UserActivityAccessibilityService
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val hasRuntimePermissions = getCallBlockPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        return hasRuntimePermissions && hasCallScreeningRole()
    }

    fun hasCallScreeningRole(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun getCallBlockPermissions(): Array<String> {
        return arrayOf(Manifest.permission.READ_CONTACTS)
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

    fun hasPinPermission(): Boolean {
        return PreferenceRepository.instance.hasPin()
    }

    fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }

    fun hasUserActivityAccessibilityPermission(): Boolean {

        val expectedComponent = ComponentName(context, UserActivityAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices
            .split(ACCESSIBILITY_SERVICE_SEPARATOR)
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it == expectedComponent }
    }

    /**
     * Gửi deeplink mở bottom sheet và chờ event kết quả từ AppEventBus.
     *
     * Quan trọng: subscribe vào AppEventBus TRƯỚC rồi mới gửi deeplink (qua onSubscription)
     * để không bao giờ bỏ lỡ event nếu bottom sheet trả kết quả quá nhanh.
     * Nếu gửi deeplink trước rồi mới subscribe, coroutine có thể treo vĩnh viễn ở first().
     */
    private suspend inline fun <reified T : AppEvent> awaitEventAfterDeeplink(
        deeplink: String,
        extras: Map<String, Any?> = emptyMap()
    ): T {

        return AppEventBus.events
            .onSubscription {
                if (extras.isEmpty()) sendDeeplink(deeplink)
                else sendDeeplink(deeplink, extras = extras)
            }
            .filterIsInstance<T>()
            .first()
    }

    private suspend fun awaitPermissionResult(deeplink: String): Boolean {

        val result = awaitEventAfterDeeplink<AppEvent.PermissionResult>(deeplink)
        return result is AppEvent.PermissionAccept
    }

    /**
     * Yêu cầu quyền Usage Stats.
     * - Nếu đã có quyền: trả về true ngay.
     * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
     * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
     */
    suspend fun requireUsageStatsPermission(): Boolean {

        if (hasUsageStatsPermission()) return true
        return awaitPermissionResult(DeepLinks.PERMISSION_USAGE_STATS)
    }

    /**
     * Yêu cầu quyền File.
     * - Nếu đã có quyền: trả về true ngay.
     * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
     * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
     */
    suspend fun requireFilePermission(): Boolean {

        if (hasFilePermission()) return true
        return awaitPermissionResult(DeepLinks.PERMISSION_FILE)
    }

    /**
     * Yêu cầu quyền Overlay (vẽ đè lên ứng dụng khác).
     * - Nếu đã có quyền: trả về true ngay.
     * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
     * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
     */
    suspend fun requireOverlayPermission(): Boolean {

        if (hasOverlayPermission()) return true
        return awaitPermissionResult(DeepLinks.PERMISSION_OVERLAY)
    }

    /**
     * Yêu cầu đặt làm launcher mặc định.
     * - Nếu đã là default: trả về true ngay.
     * - Nếu chưa: mở bottom sheet hướng dẫn, chờ kết quả từ AppEventBus.
     * @return true nếu đã là hoặc vừa được đặt làm default, false nếu user huỷ.
     */
    suspend fun requireDefaultLauncher(): Boolean {

        if (isDefaultLauncher()){
            PreferenceRepository.instance.setPendingDefaultLauncher(false)
            return true
        }

        PreferenceRepository.instance.setPendingDefaultLauncher(true)
        val result = awaitPermissionResult(DeepLinks.PERMISSION_DEFAULT_LAUNCHER)

        if (result) {
            PreferenceRepository.instance.setPendingDefaultLauncher(false)
        }

        return result
    }

    suspend fun requireCallPermission(): Boolean {

        if (hasCallPermission()) return true
        return awaitPermissionResult(DeepLinks.PERMISSION_CALL)
    }

    suspend fun requireUserActivityAccessibilityPermission(): Boolean {

        if (hasUserActivityAccessibilityPermission()) return true
        return awaitPermissionResult(DeepLinks.PERMISSION_USER_ACTIVITY_ACCESSIBILITY)
    }

    /**
     * Yêu cầu thiết lập liên hệ khẩn cấp nếu chưa có.
     * @return true nếu đã có liên hệ hoặc vừa thiết lập xong, false nếu chưa có hoặc user huỷ.
     */
    suspend fun requireEmergencyContact(): Boolean {

        val contacts = com.simple.launcher.retirement.domain.repository.ContactRepository.instance.getSelectedContacts()
        if (contacts.isNotEmpty()) return true

        val bsResult = awaitEventAfterDeeplink<AppEvent.EmergencyContactRequiredResult>(DeepLinks.EMERGENCY_CONTACT_REQUIRED)
        if (bsResult is AppEvent.EmergencyContactRequiredAccept) {

            val setupResult = awaitEventAfterDeeplink<AppEvent.ContactSetupResult>(
                DeepLinks.CONTACT_LIST,
                DeepLinks.withBackStack(DeepLinks.Extras.IS_FLOW_SETUP to true)
            )

            val a =  setupResult is AppEvent.ContactSetupAccept &&
                    com.simple.launcher.retirement.domain.repository.ContactRepository.instance.getSelectedContacts().isNotEmpty()

            return a
        }

        return false
    }

    /**
     * Yêu cầu quyền chặn cuộc gọi (runtime permissions + role CALL_SCREENING).
     * - Nếu đã có đủ: trả về true ngay.
     * - Nếu chưa: mở bottom sheet xin quyền, chờ kết quả từ AppEventBus.
     * @return true nếu đã có hoặc vừa được cấp quyền, false nếu user huỷ.
     */
    suspend fun requireCallBlockPermissions(): Boolean {

        if (hasCallBlockPermissions()) return true
        return awaitPermissionResult(DeepLinks.PERMISSION_CALL_BLOCK)
    }

    /**
     * Yêu cầu xác thực PIN (verify nếu đã có, setup nếu chưa có).
     * @return true nếu xác thực/thiết lập thành công, false nếu user huỷ.
     */
    suspend fun requirePinPermissions(): Boolean {

        val deeplink = if (hasPinPermission()) DeepLinks.PIN_VERIFY else DeepLinks.PIN_SETUP
        val result = awaitEventAfterDeeplink<AppEvent.PinResult>(deeplink)

        return result !is AppEvent.PinCancel
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

    private const val ACCESSIBILITY_SERVICE_SEPARATOR = ":"
}
