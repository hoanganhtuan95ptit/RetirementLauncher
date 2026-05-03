package com.simple.launcher.retirement.presentation.worker

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.presentation.block.BlockActivity

class AppMonitoringService : Service() {

    private val TAG = "AppMonitoringService"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var repository: AppRepositoryImpl
    
    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        repository = AppRepositoryImpl(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        handler.removeCallbacks(monitorRunnable)
        handler.post(monitorRunnable)
        return START_STICKY
    }

    private fun checkForegroundApp() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 10
        
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundPackage: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    lastForegroundPackage = event.packageName
                }
                UsageEvents.Event.KEYGUARD_SHOWN -> {
                    lastForegroundPackage = "android.keyguard"
                }
            }
        }

        // Nếu không có package nào được xác định là foreground gần đây nhất
        val foregroundPackage = lastForegroundPackage ?: return

        Log.d(TAG, "Foreground App detected: $foregroundPackage")
        
        // Danh sách các package hệ thống và launcher không được chặn
        val systemPackages = setOf(
            packageName,
            "com.android.settings",
            "com.android.systemui",
            "android",
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

        if (systemPackages.contains(foregroundPackage) || foregroundPackage.contains("launcher")) {
            return
        }

        val allowedApps = repository.getSelectedPackages()
        Log.d(TAG, "Allowed apps: $allowedApps")

        if (allowedApps.isNotEmpty() && !allowedApps.contains(foregroundPackage)) {
            Log.d(TAG, "Blocking app: $foregroundPackage")
            blockApp()
        }
    }

    private fun blockApp() {
        val intent = Intent(this, BlockActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(monitorRunnable)
        super.onDestroy()
    }
}
