package com.simple.launcher.retirement.presentation.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.block.BlockActivity

class AppMonitoringService : Service() {

    private val TAG = "AppMonitoringService"

    // HandlerThread chạy trên background thread — tránh I/O và binder calls trên main thread
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler

    private lateinit var appRepository: AppRepository
    private lateinit var prefRepository: PreferenceRepository
    private lateinit var powerManager: PowerManager

    // Tạo 1 lần trong onCreate(), tránh tạo mới mỗi 500ms trong checkForegroundApp()
    private lateinit var systemPackages: Set<String>

    // Cache UsageStatsManager — tránh getSystemService() mỗi lần poll
    private lateinit var usageStatsManager: UsageStatsManager

    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Log.d(TAG, "Service onCreate")
        appRepository = AppRepository.instance
        prefRepository = PreferenceRepository.instance
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        systemPackages = STATIC_SYSTEM_PACKAGES + packageName
        createNotificationChannel()

        // Tạo background thread riêng cho polling — không chặn main thread
        handlerThread = HandlerThread("AppMonitorThread").also { it.start() }
        handler = Handler(handlerThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (BuildConfig.DEBUG) Log.d(TAG, "Service onStartCommand")
        // Phải gọi startForeground() ngay, trước bất kỳ xử lý nào,
        // để tránh crash BackgroundServiceStartNotAllowedException trên API 26+
        startForeground(NOTIFICATION_ID, buildNotification())

        handler.removeCallbacks(monitorRunnable)
        handler.post(monitorRunnable)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Monitor",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setShowBadge(false)
                description = "App monitoring service"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.app_name))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setOngoing(true)
        .setSilent(true)
        .build()

    private fun checkForegroundApp() {
        // Nếu tính năng bị tắt trong cài đặt, tự dừng service
        if (!prefRepository.isAppBlockEnabled()) {
            stopSelf()
            return
        }

        // Nếu màn hình đang tắt, không cần kiểm tra
        if (!powerManager.isInteractive) {
            return
        }

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

        if (BuildConfig.DEBUG) Log.d(TAG, "Foreground App detected: $foregroundPackage")

        // Dùng instance field systemPackages (khởi tạo 1 lần trong onCreate)
        if (systemPackages.contains(foregroundPackage) || foregroundPackage.contains("launcher")) {
            return
        }

        // Bỏ qua các ứng dụng mặc định
        if (appRepository.isDefaultApp(foregroundPackage)) {
            return
        }

        val allowedApps = appRepository.getSelectedPackages()
        if (allowedApps.isNotEmpty() && !allowedApps.contains(foregroundPackage)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Blocking app: $foregroundPackage")
            blockApp()
        }
    }

    private fun blockApp() {
        // startActivity phải gọi với FLAG_ACTIVITY_NEW_TASK khi từ Service,
        // không phụ thuộc thread — an toàn khi gọi từ HandlerThread.
        val intent = Intent(this, BlockActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(monitorRunnable)
        handlerThread.quitSafely()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "app_monitor_channel"
        private const val NOTIFICATION_ID = 1002

        // Tập hợp tĩnh các package hệ thống — tạo một lần duy nhất khi class load.
        // packageName của app được thêm vào khi onCreate() (dynamic).
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
