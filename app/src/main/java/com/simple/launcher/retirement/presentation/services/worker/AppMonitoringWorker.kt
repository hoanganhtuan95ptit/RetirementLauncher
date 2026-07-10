package com.simple.launcher.retirement.presentation.services.worker

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
import com.simple.launcher.retirement.presentation.block.BlockActivity
import kotlinx.coroutines.flow.Flow

class AppMonitoringWorker(context: Context) : BackgroundWorker(context) {

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    private val appRepository = AppRepository.instance
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val systemPackages = STATIC_SYSTEM_PACKAGES + context.packageName

    private val monitorRunnable = object : Runnable {

        override fun run() {

            checkForegroundApp()
            handler?.postDelayed(this, MONITOR_INTERVAL_MILLIS)
        }
    }

    override fun observeEnabled(): Flow<Boolean> = PreferenceRepository.instance.isAppBlockEnabledFlow()

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

    private fun checkForegroundApp() {

        if (!powerManager.isInteractive) return

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
        if (resolvedPackage == KEYGUARD_PACKAGE) return
        if (shouldIgnorePackage(resolvedPackage)) return

        logDebug("Foreground App detected: $resolvedPackage")

        val allowedApps = appRepository.getSelectedPackages()
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

        if (packageName in systemPackages) return true
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

    companion object {

        private const val TAG = "AppMonitoringWorker"
        private const val MONITOR_THREAD_NAME = "AppMonitorThread"
        private const val MONITOR_INTERVAL_MILLIS = 500L
        private const val QUERY_WINDOW_MILLIS = 5_000L
        private const val KEYGUARD_PACKAGE = "android.keyguard"
        private const val LAUNCHER_KEYWORD = "launcher"

        val STATIC_SYSTEM_PACKAGES = setOf(
            "com.android.settings",
            "com.android.systemui",
            "android",
            "android.keyguard",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.google.android.apps.nexuslauncher",
            "com.sec.android.app.launcher",
            "com.miui.home",
            "com.oppo.launcher",
            "com.huawei.android.launcher",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller"
        )
    }
}
