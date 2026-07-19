package com.simple.launcher.retirement.data.repository

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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.domain.repository.PermissionRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.emergency.UserActivityAccessibilityService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription

class PermissionRepositoryImpl : PermissionRepository {

    private val context: Context
        get() = MainApplication.instance

    override fun hasFilePermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            return Environment.isExternalStorageManager()
        }

        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        return readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED
    }

    override fun hasUsageStatsPermission(): Boolean {

        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun hasOverlayPermission(): Boolean {

        return Settings.canDrawOverlays(context)
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

    override fun isDefaultLauncher(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            return hasHomeRole()
        }

        return isLegacyDefaultLauncher()
    }

    override fun hasContactPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasPinPermission(): Boolean {

        return PreferenceRepository.instance.hasPin()
    }

    override fun hasCallPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
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

    override suspend fun requireUsageStatsPermission(): Boolean {

        if (hasUsageStatsPermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_USAGE_STATS)
    }

    override suspend fun requireFilePermission(): Boolean {

        if (hasFilePermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_FILE)
    }

    override suspend fun requireOverlayPermission(): Boolean {

        if (hasOverlayPermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_OVERLAY)
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

    override suspend fun requireCallPermission(): Boolean {

        if (hasCallPermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_CALL)
    }

    override suspend fun requireUserActivityAccessibilityPermission(): Boolean {

        if (hasUserActivityAccessibilityPermission()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_USER_ACTIVITY_ACCESSIBILITY)
    }

    override suspend fun requireEmergencyContact(): Boolean {

        if (ContactRepository.instance.getSelectedContacts().isNotEmpty()) return true

        val result = awaitEventAfterDeeplink<AppEvent.EmergencyContactRequiredResult>(
            DeepLinks.EMERGENCY_CONTACT_REQUIRED
        )

        if (result !is AppEvent.EmergencyContactRequiredAccept) return false

        return awaitEmergencyContactSetup()
    }

    override suspend fun requireAppList(): Boolean {

        if (AppRepository.instance.getSelectedPackages().isNotEmpty()) return true

        val result = awaitEventAfterDeeplink<AppEvent.AppListRequiredResult>(
            DeepLinks.APP_LIST_REQUIRED
        )

        if (result !is AppEvent.AppListRequiredAccept) return false

        return awaitAppListSetup()
    }

    override suspend fun requireCallBlockPermissions(): Boolean {

        if (hasCallBlockPermissions()) return true

        return awaitPermissionResult(DeepLinks.PERMISSION_CALL_BLOCK)
    }

    override suspend fun requirePinPermissions(): Boolean {

        val deeplink = if (hasPinPermission()) DeepLinks.PIN_VERIFY else DeepLinks.PIN_SETUP
        val result = awaitEventAfterDeeplink<AppEvent.PinResult>(deeplink)

        return result !is AppEvent.PinCancel
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

    override suspend fun requireFileCleanupIntro(): Boolean {

        val repository = PreferenceRepository.instance
        if (!repository.isFileCleanupFirstTime()) return true

        val result = awaitEventAfterDeeplink<AppEvent.FileCleanupIntroResult>(
            DeepLinks.FILE_CLEANUP_INTRO
        )
        if (result !is AppEvent.FileCleanupIntroAccept) return false

        repository.setFileCleanupFirstTime(false)
        return true
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

    private suspend fun awaitPermissionResult(deeplink: String): Boolean {

        val result = awaitEventAfterDeeplink<AppEvent.PermissionResult>(deeplink)

        return result is AppEvent.PermissionAccept
    }

    private suspend fun awaitEmergencyContactSetup(): Boolean {

        val result = awaitEventAfterDeeplink<AppEvent.ContactSetupResult>(
            DeepLinks.CONTACT_LIST,
            DeepLinks.withBackStack(DeepLinks.Extras.IS_FLOW_SETUP to true)
        )

        return result is AppEvent.ContactSetupAccept &&
                ContactRepository.instance.getSelectedContacts().isNotEmpty()
    }

    private suspend fun awaitAppListSetup(): Boolean {

        val result = awaitEventAfterDeeplink<AppEvent.AppSetupResult>(
            DeepLinks.APP_LIST,
            DeepLinks.withBackStack(DeepLinks.Extras.IS_FLOW_SETUP to true)
        )

        return result is AppEvent.AppSetupAccept &&
                AppRepository.instance.getSelectedPackages().isNotEmpty()
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
    }
}
