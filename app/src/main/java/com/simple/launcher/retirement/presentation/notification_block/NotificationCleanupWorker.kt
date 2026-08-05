package com.simple.launcher.retirement.presentation.notification_block

import android.app.Notification
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.service.notification.StatusBarNotification
import android.util.Log
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.services.worker.BackgroundWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Worker định kỳ quét [NotificationBlockService.getActiveNotifications] và xoá
 * những notification đã tồn tại quá thời gian giữ do user cấu hình.
 *
 * Chạy trong [com.simple.launcher.retirement.presentation.services.BackgroundService]
 * cùng pattern với [com.simple.launcher.retirement.presentation.app_monitoring.AppMonitoringWorker]:
 * observeEnabled → onStart bật HandlerThread poll, onStop tắt.
 */
class NotificationCleanupWorker(context: Context) : BackgroundWorker(context) {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    private val preferenceRepository = PreferenceRepository.instance

    // Snapshot retention để đọc từ Handler thread trong runCleanupCycle().
    @Volatile
    private var retentionMillis: Long = 0L

    private val cleanupRunnable = object : Runnable {

        override fun run() {

            runCleanupCycle()
            handler?.postDelayed(this, CLEANUP_INTERVAL_MILLIS)
        }
    }

    // ── 3. Public API (overrides từ BackgroundWorker) ─────────────────────

    override fun observeEnabled(): Flow<Boolean> = combine(
        preferenceRepository.notificationBlockEnabledFlow(),
        preferenceRepository.notificationRetentionMillisFlow()
    ) { enabled, retention ->

        enabled && retention > 0L
    }

    override fun attach(scope: CoroutineScope) {

        super.attach(scope)
        // Collect retention riêng để cập nhật snapshot @Volatile — không đọc trực tiếp
        // pref trong Handler thread mỗi tick (giữ nhất quán với pattern trong
        // AppMonitoringWorker: allowedPackages).
        scope.launch {

            preferenceRepository.notificationRetentionMillisFlow().collect { millis ->

                retentionMillis = millis
            }
        }
    }

    override fun onStart() {

        if (handlerThread?.isAlive == true) return
        logDebug("onStart")

        val thread = HandlerThread(THREAD_NAME).also { it.start() }
        handlerThread = thread
        handler = Handler(thread.looper)
        handler?.post(cleanupRunnable)
    }

    override fun onStop() {

        logDebug("onStop")
        handler?.removeCallbacks(cleanupRunnable)
        handlerThread?.quitSafely()
        handler = null
        handlerThread = null
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun runCleanupCycle() {

        val retention = retentionMillis
        if (retention <= 0L) return

        val active = NotificationBlockService.getActiveNotifications(context) ?: return
        val now = System.currentTimeMillis()

        active.forEach { sbn ->

            if (shouldSkip(sbn)) return@forEach
            if (sbn.postTime + retention > now) return@forEach

            logDebug("Cleanup expired: ${sbn.packageName} key=${sbn.key}")
            NotificationBlockService.cancelNotificationByKey(sbn.key)
        }
    }

    private fun shouldSkip(sbn: StatusBarNotification): Boolean {

        // Không đụng vào notification không cho phép dismiss (foreground service,
        // media control, ongoing call...) — kể cả khi quá hạn.
        if (!sbn.isClearable) return true

        val flags = sbn.notification?.flags ?: 0
        val protectedFlags = Notification.FLAG_ONGOING_EVENT or
            Notification.FLAG_FOREGROUND_SERVICE or
            Notification.FLAG_NO_CLEAR
        return flags and protectedFlags != 0
    }

    private fun logDebug(message: String) {

        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        private const val TAG = "NotificationCleanupWorker"
        private const val THREAD_NAME = "NotificationCleanupThread"

        // 1 phút — sai số auto-dismiss so với thời điểm hết hạn thực tế tối đa
        // ~1 phút, chấp nhận được cho tính năng "giữ x giờ".
        private const val CLEANUP_INTERVAL_MILLIS = 60_000L
    }
}
