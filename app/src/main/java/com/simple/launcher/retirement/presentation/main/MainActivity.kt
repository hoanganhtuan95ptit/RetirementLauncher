package com.simple.launcher.retirement.presentation.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.databinding.ActivityMainBinding
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.onboarding.OnboardingFragment
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.worker.AppMonitoringService
import com.simple.launcher.retirement.presentation.worker.FileCleanupWorker
import com.simple.launcher.retirement.presentation.worker.FileWatcherService

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)

    override fun setupViews(savedInstanceState: Bundle?) {
        // Quét toàn bộ một lần lúc khởi động
        scheduleInitialFileCleanup()
        
        val repository = AppRepositoryImpl(this)
        
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
            
            val fragment = when {
                !repository.isOnboardingCompleted() -> OnboardingFragment()
                isHomeIntent -> HomeFragment()
                else -> SettingsFragment()
            }
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                }
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Khi nhấn nút Home
        if (Intent.ACTION_MAIN == intent.action && intent.hasCategory(Intent.CATEGORY_HOME)) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment !is HomeFragment) {
                // Xóa backstack và chuyển về HomeFragment
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
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
        val repository = AppRepositoryImpl(this)
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
