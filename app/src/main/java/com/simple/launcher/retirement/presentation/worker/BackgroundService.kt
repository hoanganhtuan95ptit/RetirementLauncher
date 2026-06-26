package com.simple.launcher.retirement.presentation.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.services.launchCollect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine

class BackgroundService : Service() {

    private val workers = mutableListOf<BackgroundWorker>()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Phải gọi startForeground() ngay trong onCreate() để tránh ForegroundServiceDidNotStartInTimeException.
        // onStartCommand() có thể đến sau vài ms — nếu workers khởi tạo chậm thì đã quá 5 giây.
        startForeground(NOTIFICATION_ID, buildNotification())

        workers += AppMonitoringWorker(this)
        workers += FileWatcherWorker(this)
        workers += EmergencyCallWorker(this)

        // Mỗi worker tự lắng nghe config và on/off tương ứng
        workers.forEach { it.attach(serviceScope) }

        // Tự dừng service khi tất cả worker đều tắt
        combine(workers.map { it.observeEnabled() }) { states -> states.any { it } }.launchCollect(serviceScope) { anyEnabled ->
            if (!anyEnabled) stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Phải gọi startForeground() ngay để tránh crash BackgroundServiceStartNotAllowedException trên API 26+
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        workers.forEach { it.detach() }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setShowBadge(false)
                description = "Background monitoring service"
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

    companion object {
        private const val CHANNEL_ID = "background_service_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, BackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BackgroundService::class.java)
            context.stopService(intent)
        }
    }
}
