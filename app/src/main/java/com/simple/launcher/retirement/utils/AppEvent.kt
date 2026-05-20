package com.simple.launcher.retirement.utils

import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.presentation.settings.SettingItem

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

    // ── Chọn app trong danh sách ────────────────────────────────────────────
    data class AppSelected(val entity: SelectableAppEntity) : AppEvent()

    // ── Chọn liên hệ trong danh sách ───────────────────────────────────────
    data class ContactSelected(val entity: SelectableContactEntity) : AppEvent()

    // ── Nhấn / toggle item trong Settings ───────────────────────────────────
    data class SettingClicked(val item: SettingItem) : AppEvent()

    // ── Performance ─────────────────────────────────────────────────────────
    data class FpsUpdated(val screenName: String, val fps: Int) : AppEvent()
}

/** Bus duy nhất để phát / nhận toàn bộ [AppEvent] trong app. */
object AppEventBus : EventBus<AppEvent>()
