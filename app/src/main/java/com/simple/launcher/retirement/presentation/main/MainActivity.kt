package com.simple.launcher.retirement.presentation.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.databinding.ActivityMainBinding
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.worker.AppMonitoringService
import com.simple.launcher.retirement.presentation.worker.FileCleanupWorker
import com.simple.launcher.retirement.presentation.worker.FileWatcherService
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.theme.ThemeColorStore

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)

    override fun setupViews(savedInstanceState: Bundle?) {
        StringResStore.load(this)
        ThemeColorStore.load(this)


        window.navigationBarColor = Color.TRANSPARENT

        // Quét toàn bộ một lần lúc khởi động
        scheduleInitialFileCleanup()
        
        val repository = AppRepository.instance
        
        // Bắt đầu lắng nghe sự thay đổi file ngầm nếu đã có quyền và được bật
        if (hasFilePermission() && repository.isFileCleanupEnabled()) {
            startFileWatcherService()
        }
        
        // Khởi động service giám sát nếu đã có quyền và được bật
        if (hasUsageStatsPermission() && hasOverlayPermission() && repository.isAppBlockEnabled()) {
            startAppMonitoringService()
        }

        if (savedInstanceState == null) {
            val isHomeIntent = intent.hasCategory(Intent.CATEGORY_HOME)
            
            val deeplink = when {
                !repository.isOnboardingCompleted() -> "app://onboarding"
                isHomeIntent -> "app://home"
                else -> "app://settings"
            }

            sendDeeplink(deeplink)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is HomeFragment) {
                    sendDeeplink("app://home")
                }
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Khi nhấn nút Home
        if (Intent.ACTION_MAIN == intent.action && intent.hasCategory(Intent.CATEGORY_HOME)) {
            Log.d("tuanha", "onNewIntent: ")
            sendDeeplink("app://home")
        }
    }

    private fun scheduleInitialFileCleanup() {
        val cleanupRequest = OneTimeWorkRequestBuilder<FileCleanupWorker>().build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "InitialFileCleanup",
            ExistingWorkPolicy.KEEP,
            cleanupRequest
        )
    }

    fun startFileWatcherService() {
        val intent = Intent(this, FileWatcherService::class.java)
        startService(intent)
    }

    override fun onResume() {
        super.onResume()
        val repository = AppRepository.instance
        // Khởi động service nếu đã có quyền và được bật
        if (hasFilePermission() && repository.isFileCleanupEnabled()) {
            startFileWatcherService()
        }
        if (hasUsageStatsPermission() && hasOverlayPermission() && repository.isAppBlockEnabled()) {
            startAppMonitoringService()
        }
    }

    private fun hasFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    fun startAppMonitoringService() {
        val intent = Intent(this, AppMonitoringService::class.java)
        startService(intent)
    }
}
