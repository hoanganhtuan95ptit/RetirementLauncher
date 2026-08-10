package com.simple.launcher.retirement.utils

import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.model.SOSConfig
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem

/**
 * Tất cả event của app được định nghĩa tập trung tại đây.
 * Sử dụng [AppEventBus] để gửi và nhận event ở bất kỳ đâu trong app.
 *
 * Cách dùng:
 *   // Gửi event:
 *   AppEventBus.post(AppEvent.PermissionAccept)
 *   AppEventBus.post(AppEvent.AppSelected(entity))
 *
 *   // Nhận event (trong Fragment / Service):
 *   AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().observe(this) { event -> ... }
 *   AppEventBus.events.filterIsInstance<AppEvent.PermissionResult>().launchCollect(fragment) { ... }
 */
sealed class AppEvent {

    // ── Permission ──────────────────────────────────────────────────────────
    sealed class PermissionResult : AppEvent()
    object PermissionAccept : PermissionResult()
    object PermissionCancel : PermissionResult()


    sealed class PinResult : AppEvent()
    object PinCancel : PinResult()
    object PinSetupSuccess : PinResult()
    object PinVerifySuccess : PinResult()

    // ── App Monitoring Intro ────────────────────────────────────────────────
    sealed class AppMonitoringIntroResult : AppEvent()
    object AppMonitoringIntroAccept : AppMonitoringIntroResult()
    object AppMonitoringIntroCancel : AppMonitoringIntroResult()

    sealed class EmergencyCallIntroResult : AppEvent()
    object EmergencyCallIntroAccept : EmergencyCallIntroResult()
    object EmergencyCallIntroCancel : EmergencyCallIntroResult()

    sealed class EmergencyContactRequiredResult : AppEvent()
    object EmergencyContactRequiredAccept : EmergencyContactRequiredResult()
    object EmergencyContactRequiredCancel : EmergencyContactRequiredResult()

    sealed class AppListRequiredResult : AppEvent()
    object AppListRequiredAccept : AppListRequiredResult()
    object AppListRequiredCancel : AppListRequiredResult()

    sealed class ContactSetupResult : AppEvent()
    object ContactSetupAccept : ContactSetupResult()
    object ContactSetupCancel : ContactSetupResult()

    sealed class AppSetupResult : AppEvent()
    object AppSetupAccept : AppSetupResult()
    object AppSetupCancel : AppSetupResult()


    sealed class CallBlockIntroResult : AppEvent()
    object CallBlockIntroAccept : CallBlockIntroResult()
    object CallBlockIntroCancel : CallBlockIntroResult()

    // ── Chọn app trong danh sách ────────────────────────────────────────────
    data class AppSelected(val entity: SelectableAppEntity) : AppEvent()

    // ── Toggle app trong danh sách uninstall ────────────────────────────────
    data class UninstallAppToggled(val entity: AppEntity) : AppEvent()

    // ── Chọn liên hệ trong danh sách ───────────────────────────────────────
    data class ContactSelected(val entity: SelectableContactEntity) : AppEvent()

    // ── Nhấn / toggle item trong Settings ───────────────────────────────────
    data class SettingClicked(val item: SettingItem) : AppEvent()

    data class SOSItemClicked(val id: Int) : AppEvent()

    data class SOSTimeoutSelected(val timeoutMillis: Long) : AppEvent()

    // ── Notification Block ──────────────────────────────────────────────────
    data class NotificationBlockAppToggled(val entity: AppEntity) : AppEvent()

    data class NotificationRetentionSelected(val retentionMillis: Long) : AppEvent()

    object NotificationBlockHeaderClicked : AppEvent()

    data class SOSUpdate(
        val config: SOSConfig
    ) : AppEvent()

    object SOSUpdateSuccess : AppEvent()
    object SOSUpdateCancel : AppEvent()

    // ── Emergency Alert (màn A xác nhận an toàn) ───────────────────────────
    sealed class EmergencyAlertResult : AppEvent()
    object EmergencyAlertConfirmedSafe : EmergencyAlertResult()
    object EmergencyAlertTimedOut : EmergencyAlertResult()

    // ── Performance ─────────────────────────────────────────────────────────
    data class FpsUpdated(val screenName: String, val fps: Int) : AppEvent()
}

/** Bus duy nhất để phát / nhận toàn bộ [AppEvent] trong app. */
object AppEventBus : EventBus<AppEvent>()
