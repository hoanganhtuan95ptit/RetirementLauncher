package com.simple.launcher.retirement.presentation.worker

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.os.Build
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.presentation.block.BlockActivity

class AppMonitoringService : Service() {

    private val TAG = "AppMonitoringService"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var repository: AppRepository
    private lateinit var powerManager: PowerManager
    
    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        repository = AppRepository.instance
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        handler.removeCallbacks(monitorRunnable)
        handler.post(monitorRunnable)
        return START_STICKY
    }

    private fun checkForegroundApp() {
        // Nếu tính năng bị tắt trong cài đặt, tự dừng service
        if (!repository.isAppBlockEnabled()) {
            stopSelf()
            return
        }

        // Nếu màn hình đang tắt, không cần kiểm tra
        if (!powerManager.isInteractive) {
            return
        }

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 5
        
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundPackage: String? = null
        var isKeyguardVisible = false

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    lastForegroundPackage = event.packageName
                }
                UsageEvents.Event.KEYGUARD_SHOWN -> {
                    isKeyguardVisible = true
                }
                UsageEvents.Event.KEYGUARD_HIDDEN -> {
                    isKeyguardVisible = false
                }
            }
        }

        // Nếu màn hình khóa đang hiện, không chặn
        if (isKeyguardVisible) return

        // Nếu không có package nào được xác định là foreground gần đây nhất
        val foregroundPackage = lastForegroundPackage ?: return

        if (foregroundPackage == "android.keyguard") return

        Log.d(TAG, "Foreground App detected: $foregroundPackage")
        
        // Danh sách các package hệ thống và launcher không được chặn
        val systemPackages = setOf(
            packageName,
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

        if (systemPackages.contains(foregroundPackage) || foregroundPackage.contains("launcher")) {
            return
        }

        // Bỏ qua các ứng dụng mặc định
        if (repository.isDefaultApp(foregroundPackage)) {
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
