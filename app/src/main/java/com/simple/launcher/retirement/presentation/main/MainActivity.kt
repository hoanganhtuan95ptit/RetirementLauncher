package com.simple.launcher.retirement.presentation.main

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ActivityMainBinding
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.worker.BackgroundService
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.theme.ThemeColorStore
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

        val repository = PreferenceRepository.instance

        // Khởi động BackgroundService nếu ít nhất một tính năng được bật và có đủ quyền
        val hasFilePerm = PermissionManager.hasFilePermission()
        val fileCleanupEnabled = repository.isFileCleanupEnabled()
        val hasUsagePerm = PermissionManager.hasUsageStatsPermission()
        val hasOverlayPerm = PermissionManager.hasOverlayPermission()
        val appBlockEnabled = repository.isAppBlockEnabled()
        Log.d(TAG, "BackgroundService | hasFilePerm=$hasFilePerm | fileCleanupEnabled=$fileCleanupEnabled | hasUsagePerm=$hasUsagePerm | appBlockEnabled=$appBlockEnabled")
        if ((hasFilePerm && fileCleanupEnabled) || (hasUsagePerm && hasOverlayPerm && appBlockEnabled)) {
            startBackgroundService()
        }

        if (savedInstanceState == null) {
            val isOnboardingCompleted = repository.isOnboardingCompleted()
            val isHomeIntent = intent.hasCategory(Intent.CATEGORY_HOME)

            val deeplink = when {
                !isOnboardingCompleted -> DeepLinks.ONBOARDING
                isHomeIntent -> DeepLinks.HOME
                else -> DeepLinks.SETTINGS
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
                    sendDeeplink(DeepLinks.HOME)
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
            sendDeeplink(DeepLinks.HOME)
        }
    }

    fun startBackgroundService() {
        Log.d(TAG, "startBackgroundService")
        BackgroundService.start(this)
    }

    override fun onResume() {
        super.onResume()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        Log.d(TAG, "onResume | currentFragment=${currentFragment?.javaClass?.simpleName}")
        val repository = PreferenceRepository.instance
        val shouldStart = (PermissionManager.hasFilePermission() && repository.isFileCleanupEnabled())
            || (PermissionManager.hasUsageStatsPermission() && PermissionManager.hasOverlayPermission() && repository.isAppBlockEnabled())
        if (shouldStart) startBackgroundService()
    }

    companion object {
        private const val TAG = "tuanha"
    }
}
