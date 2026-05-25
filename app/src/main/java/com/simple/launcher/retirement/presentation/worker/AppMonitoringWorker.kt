package com.simple.launcher.retirement.presentation.worker

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

    private val TAG = "AppMonitoringWorker"

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    private val appRepository = AppRepository.instance
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val systemPackages = STATIC_SYSTEM_PACKAGES + context.packageName

    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler?.postDelayed(this, 500)
        }
    }

    override fun observeEnabled(): Flow<Boolean> = PreferenceRepository.instance.isAppBlockEnabledFlow()

    override fun onStart() {
        if (handlerThread?.isAlive == true) return
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart")
        handlerThread = HandlerThread("AppMonitorThread").also { it.start() }
        handler = Handler(handlerThread!!.looper)
        handler?.post(monitorRunnable)
    }

    override fun onStop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStop")
        handler?.removeCallbacks(monitorRunnable)
        handlerThread?.quitSafely()
        handler = null
        handlerThread = null
    }

    private fun checkForegroundApp() {
        if (!powerManager.isInteractive) return

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 5

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundPackage: String? = null
        var isKeyguardVisible = false

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> lastForegroundPackage = event.packageName
                UsageEvents.Event.KEYGUARD_SHOWN -> isKeyguardVisible = true
                UsageEvents.Event.KEYGUARD_HIDDEN -> isKeyguardVisible = false
            }
        }

        if (isKeyguardVisible) return

        val foregroundPackage = lastForegroundPackage ?: return
        if (foregroundPackage == "android.keyguard") return

        if (BuildConfig.DEBUG) Log.d(TAG, "Foreground App detected: $foregroundPackage")

        if (systemPackages.contains(foregroundPackage) || foregroundPackage.contains("launcher")) return
        if (appRepository.isDefaultApp(foregroundPackage)) return

        val allowedApps = appRepository.getSelectedPackages()
        if (allowedApps.isNotEmpty() && !allowedApps.contains(foregroundPackage)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Blocking app: $foregroundPackage")
            blockApp(foregroundPackage)
        }
    }

    private fun blockApp(packageName: String) {
        val appLabel = try {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: Exception) {
            null
        }

        val intent = Intent(context, BlockActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .apply { if (appLabel != null) putExtra(BlockActivity.EXTRA_APP_NAME, appLabel) }
        context.startActivity(intent)
    }

    companion object {
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
