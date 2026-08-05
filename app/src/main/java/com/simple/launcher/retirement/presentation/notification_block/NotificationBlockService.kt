package com.simple.launcher.retirement.presentation.notification_block

import android.app.Notification
import android.content.ComponentName
import android.content.Context
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
 * Lắng nghe notification hệ thống để CHẶN (dismiss) thông báo của các app trong
 * blacklist ngay khi vừa post.
 *
 * Việc dọn dẹp thông báo hết hạn KHÔNG do service tự xử lý — do
 * [NotificationCleanupWorker] (BackgroundWorker chạy trong BackgroundService) poll
 * định kỳ. Service chỉ đóng vai trò bridge tĩnh vì `activeNotifications` /
 * `cancelNotification(key)` chỉ có thể gọi từ instance NotificationListenerService.
 *
 * Người dùng phải cấp quyền "Notification Access" thì service mới nhận được callback.
 */
class NotificationBlockService : NotificationListenerService() {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private val preferenceRepository = PreferenceRepository.instance

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var configJob: Job? = null

    private var isConnected: Boolean = false
    private var isFeatureEnabled: Boolean = false
    private var blockedPackages: Set<String> = emptySet()

    // ── 3. Public API ─────────────────────────────────────────────────────

    override fun onListenerConnected() {

        super.onListenerConnected()
        isConnected = true
        instance = this
        observeConfig()
    }

    override fun onListenerDisconnected() {

        configJob?.cancel()
        configJob = null
        isConnected = false
        if (instance === this) instance = null
        super.onListenerDisconnected()
    }

    override fun onDestroy() {

        configJob?.cancel()
        serviceScope.cancel()
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        sbn ?: return
        if (!isConnected || !isFeatureEnabled) return
        if (shouldSkip(sbn)) return
        if (sbn.packageName !in blockedPackages) return
        dismissNotification(sbn)
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun observeConfig() {

        configJob?.cancel()
        configJob = combine(
            preferenceRepository.notificationBlockEnabledFlow(),
            preferenceRepository.notificationBlockedPackagesFlow()
        ) { enabled, blocked ->

            enabled to blocked
        }.onEach { (enabled, blocked) ->

            isFeatureEnabled = enabled
            blockedPackages = blocked
            sweepBlockedFromActive()
        }.launchIn(serviceScope)
    }

    /**
     * Duyệt lại notification hiện có để bắt kịp thay đổi blacklist / trạng thái
     * enable — cần thiết khi user vừa bật feature hoặc thêm app vào blacklist
     * mà thông báo đã hiển thị từ trước.
     */
    private fun sweepBlockedFromActive() {

        if (!isConnected || !isFeatureEnabled) return
        val active = runCatching { activeNotifications }.getOrNull() ?: return
        active.forEach { sbn ->

            if (shouldSkip(sbn)) return@forEach
            if (sbn.packageName in blockedPackages) dismissNotification(sbn)
        }
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

    // ── 6. Companion object (bridge cho NotificationCleanupWorker) ───────

    companion object {

        @Volatile
        private var instance: NotificationBlockService? = null

        /**
         * Snapshot notification đang hiển thị.
         * - Trả `null` nếu service chưa bind (process bị kill, permission chưa cấp,
         *   listener chưa reconnect). Trong trường hợp này ta gọi
         *   [NotificationListenerService.requestRebind] để lần tick worker kế tiếp
         *   có instance sẵn.
         */
        fun getActiveNotifications(context: Context): Array<StatusBarNotification>? {

            val alive = instance
            if (alive == null) {

                requestRebindSilently(context)
                return null
            }
            return runCatching { alive.activeNotifications }.getOrNull()
        }

        /**
         * Xoá notification theo key. No-op nếu service chưa bind.
         */
        fun cancelNotificationByKey(key: String) {

            instance?.let { runCatching { it.cancelNotification(key) } }
        }

        private fun requestRebindSilently(context: Context) {

            runCatching {

                val component = ComponentName(
                    context.applicationContext,
                    NotificationBlockService::class.java
                )
                NotificationListenerService.requestRebind(component)
            }
        }
    }
}
