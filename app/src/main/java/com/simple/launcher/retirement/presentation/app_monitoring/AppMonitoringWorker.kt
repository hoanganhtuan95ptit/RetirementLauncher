package com.simple.launcher.retirement.presentation.app_monitoring

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.util.Log
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.app_block.BlockActivity
import com.simple.launcher.retirement.presentation.services.worker.BackgroundWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AppMonitoringWorker(context: Context) : BackgroundWorker(context) {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    private var resolvedPackageCurrent: String? = null

    // Snapshot của selected packages, cập nhật liên tục từ getSelectedPackagesFlow().
    // Đọc từ Handler thread trong checkForegroundApp() nên phải @Volatile.
    @Volatile
    private var allowedPackages: List<String> = emptyList()

    private val appRepository = AppRepository.instance
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val monitorRunnable = object : Runnable {

        override fun run() {

            checkForegroundApp()
            handler?.postDelayed(this, MONITOR_INTERVAL_MILLIS)
        }
    }

    // ── 3. Public API (overrides từ BackgroundWorker) ─────────────────────

    override fun observeEnabled(): Flow<Boolean> = PreferenceRepository.instance.appBlockEnabledFlow()

    override fun attach(scope: CoroutineScope) {

        super.attach(scope)
        // Collect selected packages theo dõi thay đổi thay vì đọc đồng bộ mỗi lần poll.
        scope.launch {

            appRepository.getSelectedPackagesFlow().collect { packages ->

                allowedPackages = packages
            }
        }
    }

    override fun onStart() {

        if (handlerThread?.isAlive == true) return
        logDebug("onStart")

        val monitoringThread = HandlerThread(MONITOR_THREAD_NAME).also { it.start() }
        handlerThread = monitoringThread
        handler = Handler(monitoringThread.looper)
        handler?.post(monitorRunnable)
    }

    override fun onStop() {

        logDebug("onStop")
        handler?.removeCallbacks(monitorRunnable)
        handlerThread?.quitSafely()
        handler = null
        handlerThread = null
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun checkForegroundApp() {

        // Tắt màn hình hoặc đang sleep thì không cần poll foreground app.
        if (!powerManager.isInteractive) return

        // Có luồng đặc biệt đang tạm dừng monitoring (ví dụ user đang ở màn Xoá app
        // → không được chặn packageinstaller). Reset "current" để khi resume, package
        // đầu tiên phát hiện lại vẫn được đánh giá lại thay vì bị bỏ qua do trùng.
        if (AppMonitoringPauser.isPaused) {

            resolvedPackageCurrent = null
            return
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - QUERY_WINDOW_MILLIS

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var foregroundPackage: String? = null
        var isKeyguardVisible = false

        while (usageEvents.hasNextEvent()) {

            usageEvents.getNextEvent(event)
            when (event.eventType) {

                UsageEvents.Event.ACTIVITY_RESUMED -> foregroundPackage = event.packageName
                UsageEvents.Event.KEYGUARD_SHOWN -> isKeyguardVisible = true
                UsageEvents.Event.KEYGUARD_HIDDEN -> isKeyguardVisible = false
            }
        }

        if (isKeyguardVisible) return

        val resolvedPackage = foregroundPackage ?: return

        if (resolvedPackageCurrent == resolvedPackage) return
        resolvedPackageCurrent = resolvedPackage

        if (shouldIgnorePackage(resolvedPackage)) return

        logDebug("Foreground App detected: $resolvedPackage")

        val allowedApps = allowedPackages
        if (allowedApps.isEmpty()) return
        if (allowedApps.contains(resolvedPackage)) return

        logDebug("Blocking app: $resolvedPackage")
        blockApp(resolvedPackage)
    }

    private fun blockApp(packageName: String) {

        val appLabel = getAppLabel(packageName)

        val intent = Intent(context, BlockActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .apply { if (appLabel != null) putExtra(BlockActivity.EXTRA_APP_NAME, appLabel) }
        context.startActivity(intent)
    }

    private fun shouldIgnorePackage(packageName: String): Boolean {

        // Bỏ qua app hệ thống, launcher OEM, và mọi package đang được OS coi là "default app".
        if (packageName == context.packageName) return true
        if (packageName.contains(LAUNCHER_KEYWORD, ignoreCase = true)) return true

        return appRepository.isDefaultApp(packageName)
    }

    private fun getAppLabel(packageName: String): String? {

        return try {

            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (_: Exception) {

            null
        }
    }

    private fun logDebug(message: String) {

        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        private const val TAG = "AppMonitoringWorker"
        private const val MONITOR_THREAD_NAME = "AppMonitorThread"
        private const val MONITOR_INTERVAL_MILLIS = 500L
        private const val QUERY_WINDOW_MILLIS = 5_000L
        private const val LAUNCHER_KEYWORD = "launcher"
    }
}
