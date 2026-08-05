package com.simple.launcher.retirement.data.repository

import android.Manifest
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.emergency.UserActivityAccessibilityService
import com.simple.launcher.retirement.presentation.notification_block.NotificationBlockService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription

class PermissionRepositoryImpl(private val context: Context) : PermissionRepository {

    override fun hasUsageStatsPermission(): Boolean {

        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }

    override suspend fun requireUsageStatsPermission(): Boolean {

        if (hasUsageStatsPermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_USAGE_STATS)
    }

    override fun hasOverlayPermission(): Boolean {

        return Settings.canDrawOverlays(context)
    }

    override suspend fun requireOverlayPermission(): Boolean {

        if (hasOverlayPermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_OVERLAY)
    }

    override fun isDefaultLauncher(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            return hasHomeRole()
        }

        return isLegacyDefaultLauncher()
    }

    override suspend fun requireDefaultLauncher(): Boolean {

        if (isDefaultLauncher()) {

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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hasHomeRole(): Boolean {

        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager

        return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
    }

    private fun isLegacyDefaultLauncher(): Boolean {

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)

        val resolveInfo = context.packageManager.resolveActivity(intent, 0)

        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    override fun hasCallPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requireCallPermission(): Boolean {

        if (hasCallPermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_CALL)
    }

    override fun hasUserActivityAccessibilityPermission(): Boolean {

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

    override suspend fun requireUserActivityAccessibilityPermission(): Boolean {

        if (hasUserActivityAccessibilityPermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_USER_ACTIVITY_ACCESSIBILITY)
    }

    override fun hasNotificationListenerAccess(): Boolean {

        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            SETTING_ENABLED_NOTIFICATION_LISTENERS
        ) ?: return false

        val expectedComponent = ComponentName(context, NotificationBlockService::class.java)

        return enabledListeners
            .split(NOTIFICATION_LISTENER_SEPARATOR)
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it == expectedComponent }
    }

    override suspend fun requireNotificationListenerAccess(): Boolean {

        if (hasNotificationListenerAccess()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_NOTIFICATION_LISTENER)
    }

    override fun hasCallBlockPermissions(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val hasRuntimePermissions = getCallBlockPermissions().all {

            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        return hasRuntimePermissions && hasCallScreeningRole()
    }

    override fun hasCallScreeningRole(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager

        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    override fun getCallBlockPermissions(): Array<String> {

        return arrayOf(Manifest.permission.READ_CONTACTS)
    }

    override suspend fun requireCallBlockPermissions(): Boolean {

        if (hasCallBlockPermissions()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_CALL_BLOCK)
    }

    override suspend fun requireCallBlockIntro(): Boolean {

        val repository = PreferenceRepository.instance
        if (!repository.isCallBlockFirstTime()) return true

        val result = awaitEventAfterDeeplink<AppEvent.CallBlockIntroResult>(
            DeepLinks.CALL_BLOCK_INTRO
        )
        if (result !is AppEvent.CallBlockIntroAccept) return false

        repository.setCallBlockFirstTime(false)
        return true
    }

    override fun hasContactPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requireEmergencyContact(): Boolean {

        if (ContactRepository.instance.getSelectedContactsFlow().first().isNotEmpty()) return true

        val result = awaitEventAfterDeeplink<AppEvent.EmergencyContactRequiredResult>(
            DeepLinks.EMERGENCY_CONTACT_REQUIRED
        )

        if (result !is AppEvent.EmergencyContactRequiredAccept) return false

        return awaitEmergencyContactSetup()
    }

    private suspend fun awaitEmergencyContactSetup(): Boolean {

        val result = awaitEventAfterDeeplink<AppEvent.ContactSetupResult>(
            DeepLinks.CONTACT_LIST,
            DeepLinks.withBackStack(DeepLinks.Extras.IS_FLOW_SETUP to true)
        )

        return result is AppEvent.ContactSetupAccept &&
                ContactRepository.instance.getSelectedContactsFlow().first().isNotEmpty()
    }

    override suspend fun requireAppList(): Boolean {

        if (AppRepository.instance.getSelectedPackagesFlow().first().isNotEmpty()) return true

        val result = awaitEventAfterDeeplink<AppEvent.AppListRequiredResult>(
            DeepLinks.APP_LIST_REQUIRED
        )

        if (result !is AppEvent.AppListRequiredAccept) return false

        return awaitAppListSetup()
    }

    private suspend fun awaitAppListSetup(): Boolean {

        val result = awaitEventAfterDeeplink<AppEvent.AppSetupResult>(
            DeepLinks.APP_LIST,
            DeepLinks.withBackStack(DeepLinks.Extras.IS_FLOW_SETUP to true)
        )

        return result is AppEvent.AppSetupAccept &&
                AppRepository.instance.getSelectedPackagesFlow().first().isNotEmpty()
    }

    override fun hasPinPermission(): Boolean {

        return PreferenceRepository.instance.hasPin()
    }

    override suspend fun requirePinPermissions(): Boolean {

        return true // todo tắt tính năng pin
//        val deeplink = if (hasPinPermission()) DeepLinks.PIN_VERIFY else DeepLinks.PIN_SETUP
//        val result = awaitEventAfterDeeplink<AppEvent.PinResult>(deeplink)
//
//        return result !is AppEvent.PinCancel
    }

    override suspend fun requireAppMonitoringIntro(): Boolean {

        val repository = PreferenceRepository.instance
        if (!repository.isAppBlockFirstTime()) return true

        val result = awaitEventAfterDeeplink<AppEvent.AppMonitoringIntroResult>(
            DeepLinks.APP_MONITORING_INTRO
        )
        if (result !is AppEvent.AppMonitoringIntroAccept) return false

        repository.setAppBlockFirstTime(false)
        return true
    }

    override suspend fun requireEmergencyCallIntro(): Boolean {

        val repository = PreferenceRepository.instance
        if (!repository.isEmergencyCallFirstTime()) return true

        val result = awaitEventAfterDeeplink<AppEvent.EmergencyCallIntroResult>(
            DeepLinks.EMERGENCY_CALL_INTRO
        )
        if (result !is AppEvent.EmergencyCallIntroAccept) return false

        repository.setEmergencyCallFirstTime(false)
        return true
    }

    private suspend fun awaitPermissionResult(deeplink: String): Boolean {

        val result = awaitEventAfterDeeplink<AppEvent.PermissionResult>(deeplink)

        return result is AppEvent.PermissionAccept
    }

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

    private companion object {

        const val ACCESSIBILITY_SERVICE_SEPARATOR = ":"
        const val NOTIFICATION_LISTENER_SEPARATOR = ":"
        const val SETTING_ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    }
}
