package com.simple.launcher.retirement.presentation.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.app_monitoring.AppMonitoringWorker
import com.simple.launcher.retirement.presentation.emergency.EmergencyWorker
import com.simple.launcher.retirement.presentation.installer_cleanup.InstallerCleanupWorker
import com.simple.launcher.retirement.presentation.services.worker.BackgroundWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine

class BackgroundService : Service() {

    private val workers: List<BackgroundWorker> by lazy {

        listOf(
            AppMonitoringWorker(this),
            InstallerCleanupWorker(this),
            EmergencyWorker(this)
        )
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {

        super.onCreate()
        startAsForegroundService()
        attachWorkers()
        observeWorkerStates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startAsForegroundService()
        return START_STICKY
    }

    override fun onDestroy() {

        workers.forEach { it.detach() }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun attachWorkers() {

        workers.forEach { it.attach(serviceScope) }
    }

    private fun observeWorkerStates() {

        // Service chỉ tồn tại khi ít nhất một feature nền vẫn đang được bật.
        combine(workers.map { it.observeEnabled() }) { states ->

            states.any { it }
        }.launchCollect(serviceScope) { anyEnabled ->

            if (!anyEnabled) stopSelf()
        }
    }

    private fun startAsForegroundService() {

        // Gọi lại an toàn ở cả onCreate/onStartCommand để tránh timeout khi Android khởi service nền.
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN
        ).apply {

            setShowBadge(false)
            description = CHANNEL_DESCRIPTION
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
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
        private const val CHANNEL_NAME = "Background Service"
        private const val CHANNEL_DESCRIPTION = "Background monitoring service"
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
