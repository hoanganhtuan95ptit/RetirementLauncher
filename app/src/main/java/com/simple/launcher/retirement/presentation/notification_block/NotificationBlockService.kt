package com.simple.launcher.retirement.presentation.notification_block

import android.app.Notification
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Lắng nghe toàn bộ notification hệ thống để:
 * 1. Chặn ngay (dismiss) thông báo của các app nằm trong danh sách [PreferenceRepository.getNotificationBlockedPackages].
 * 2. Tự động xoá thông báo nếu tồn tại quá [PreferenceRepository.getNotificationRetentionMillis].
 *
 * Người dùng phải cấp quyền "Notification Access" thì service mới nhận được callback.
 * Khi cấu hình thay đổi, service reschedule lại toàn bộ notification hiện có.
 */
class NotificationBlockService : NotificationListenerService() {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private val preferenceRepository = PreferenceRepository.instance

    // Handler chạy trên main thread — cancelable Runnable là cách nhẹ nhất để hẹn giờ xoá
    // một notification cụ thể, không cần WorkManager cho tác vụ ephemeral này.
    private val handler = Handler(Looper.getMainLooper())

    // Key = notification key duy nhất (packageName + id + tag + user). Value = Runnable đang chờ.
    private val pendingRemovals = HashMap<String, Runnable>()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var configJob: Job? = null

    private var isConnected: Boolean = false
    private var isFeatureEnabled: Boolean = false
    private var blockedPackages: Set<String> = emptySet()
    private var retentionMillis: Long = 0L

    // ── 3. Public API ─────────────────────────────────────────────────────

    override fun onListenerConnected() {

        super.onListenerConnected()
        isConnected = true
        observeConfig()
    }

    override fun onListenerDisconnected() {

        cancelAllPendingRemovals()
        configJob?.cancel()
        configJob = null
        isConnected = false
        super.onListenerDisconnected()
    }

    override fun onDestroy() {

        cancelAllPendingRemovals()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        sbn ?: return
        if (!isConnected || !isFeatureEnabled) return
        if (shouldSkip(sbn)) return

        if (sbn.packageName in blockedPackages) {

            dismissNotification(sbn)
            return
        }

        scheduleRemovalIfNeeded(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {

        sbn ?: return
        cancelPendingRemoval(sbn.key)
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun observeConfig() {

        configJob?.cancel()
        configJob = combine(
            preferenceRepository.notificationBlockEnabledFlow(),
            preferenceRepository.notificationBlockedPackagesFlow(),
            preferenceRepository.notificationRetentionMillisFlow()
        ) { enabled, blocked, retention ->

            Triple(enabled, blocked, retention)
        }.onEach { (enabled, blocked, retention) ->

            isFeatureEnabled = enabled
            blockedPackages = blocked
            retentionMillis = retention
            applyConfigToActiveNotifications()
        }.launchIn(serviceScope)
    }

    private fun applyConfigToActiveNotifications() {

        cancelAllPendingRemovals()
        if (!isFeatureEnabled) return

        val active = runCatching { activeNotifications }.getOrNull() ?: return

        active.forEach { sbn ->

            if (shouldSkip(sbn)) return@forEach

            if (sbn.packageName in blockedPackages) {

                dismissNotification(sbn)
            } else {

                scheduleRemovalIfNeeded(sbn)
            }
        }
    }

    private fun scheduleRemovalIfNeeded(sbn: StatusBarNotification) {

        val retention = retentionMillis
        if (retention <= 0L) return

        val elapsed = System.currentTimeMillis() - sbn.postTime
        val delay = (retention - elapsed).coerceAtLeast(0L)

        cancelPendingRemoval(sbn.key)

        val runnable = Runnable {

            pendingRemovals.remove(sbn.key)
            dismissNotification(sbn)
        }
        pendingRemovals[sbn.key] = runnable
        handler.postDelayed(runnable, delay)
    }

    private fun cancelPendingRemoval(key: String) {

        val runnable = pendingRemovals.remove(key) ?: return
        handler.removeCallbacks(runnable)
    }

    private fun cancelAllPendingRemovals() {

        pendingRemovals.values.forEach { handler.removeCallbacks(it) }
        pendingRemovals.clear()
    }

    private fun dismissNotification(sbn: StatusBarNotification) {

        runCatching { cancelNotification(sbn.key) }
    }

    private fun shouldSkip(sbn: StatusBarNotification): Boolean {

        // Không đụng vào các notification không cho phép user dismiss (foreground service,
        // media control, ongoing call...) để tránh xung đột với hệ thống.
        if (!sbn.isClearable) return true

        val flags = sbn.notification?.flags ?: 0
        val protectedFlags = Notification.FLAG_ONGOING_EVENT or
            Notification.FLAG_FOREGROUND_SERVICE or
            Notification.FLAG_NO_CLEAR
        return flags and protectedFlags != 0
    }
}
