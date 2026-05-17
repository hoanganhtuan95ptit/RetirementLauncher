package com.simple.launcher.retirement.presentation.main

import android.app.ActivityManager
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
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ActivityMainBinding
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.worker.AppMonitoringService
import com.simple.launcher.retirement.presentation.worker.FileCleanupWorker
import com.simple.launcher.retirement.presentation.worker.FileWatcherService
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.theme.ThemeColorStore
import androidx.core.view.doOnLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)

    override fun setupViews(savedInstanceState: Bundle?) {
        Log.d(TAG, "setupViews | savedInstanceState=${savedInstanceState != null} | action=${intent.action} | categories=${intent.categories}")

        // Chuyển reflection-based loading sang IO thread để không chặn main thread khi khởi động.
        // ViewModel sẽ nhận emptyMap() ban đầu rồi cập nhật lại khi load xong (qua StateFlow).
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                StringResStore.load(this@MainActivity)
                ThemeColorStore.load(this@MainActivity)
            }
        }

        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Quét toàn bộ một lần lúc khởi động
        scheduleInitialFileCleanup()

        val repository = PreferenceRepository.instance

        // Bắt đầu lắng nghe sự thay đổi file ngầm nếu đã có quyền và được bật
        val hasFilePerm = PermissionManager.hasFilePermission(this)
        val fileCleanupEnabled = repository.isFileCleanupEnabled()
        Log.d(TAG, "FileWatcher | hasFilePerm=$hasFilePerm | fileCleanupEnabled=$fileCleanupEnabled")
        if (hasFilePerm && fileCleanupEnabled) {
            startFileWatcherService()
        }

        // Khởi động service giám sát nếu đã có quyền và được bật
        val hasUsagePerm = PermissionManager.hasUsageStatsPermission(this)
        val hasOverlayPerm = PermissionManager.hasOverlayPermission(this)
        val appBlockEnabled = repository.isAppBlockEnabled()
        Log.d(TAG, "AppMonitoring | hasUsagePerm=$hasUsagePerm | hasOverlayPerm=$hasOverlayPerm | appBlockEnabled=$appBlockEnabled")
        if (hasUsagePerm && hasOverlayPerm && appBlockEnabled) {
            startAppMonitoringService()
        }

        if (savedInstanceState == null) {
            val isOnboardingCompleted = repository.isOnboardingCompleted()
            val isHomeIntent = intent.hasCategory(Intent.CATEGORY_HOME)

            val deeplink = when {
                !isOnboardingCompleted -> "app://onboarding"
                isHomeIntent -> "app://home"
                else -> "app://settings"
            }

            Log.d(TAG, "navigate | isOnboardingCompleted=$isOnboardingCompleted | isHomeIntent=$isHomeIntent | deeplink=$deeplink")
            // Dùng doOnLayout để đảm bảo container đã được measure/layout trước khi
            // commit fragment transaction — tránh race condition với onCreate.
            binding.root.doOnLayout {
                lifecycleScope.launch { sendDeeplink(deeplink) }
            }
        } else {
            Log.d(TAG, "navigate | skipped — restoring from savedInstanceState")
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val backstackCount = supportFragmentManager.backStackEntryCount
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                Log.d(TAG, "onBackPressed | backStackCount=$backstackCount | currentFragment=${currentFragment?.javaClass?.simpleName}")
                if (backstackCount > 0) {
                    supportFragmentManager.popBackStack()
                } else if (intent.hasCategory(Intent.CATEGORY_HOME) && currentFragment !is HomeFragment) {
                    sendDeeplink("app://home")
                } else {
                    finish()
                }
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent | action=${intent.action} | categories=${intent.categories}")
        // Khi nhấn nút Home
        if (Intent.ACTION_MAIN == intent.action && intent.hasCategory(Intent.CATEGORY_HOME)) {
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
        if (isServiceRunning(FileWatcherService::class.java)) return
        Log.d(TAG, "startFileWatcherService")
        val intent = Intent(this, FileWatcherService::class.java)
        startService(intent)
    }

    fun startAppMonitoringService() {
        if (isServiceRunning(AppMonitoringService::class.java)) return
        Log.d(TAG, "startAppMonitoringService")
        val intent = Intent(this, AppMonitoringService::class.java)
        startService(intent)
    }

    /** Kiểm tra service có đang chạy không để tránh restart không cần thiết mỗi onResume. */
    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    override fun onResume() {
        super.onResume()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        Log.d(TAG, "onResume | currentFragment=${currentFragment?.javaClass?.simpleName}")
        val repository = PreferenceRepository.instance
        if (PermissionManager.hasFilePermission(this) && repository.isFileCleanupEnabled()) {
            startFileWatcherService()
        }
        if (PermissionManager.hasUsageStatsPermission(this) && PermissionManager.hasOverlayPermission(this) && repository.isAppBlockEnabled()) {
            startAppMonitoringService()
        }
    }

    companion object {
        private const val TAG = "tuanha"
    }
}
