package com.simple.launcher.retirement.presentation.main

import android.content.Intent
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

        val repository = PreferenceRepository.instance

        preloadResourceStores()
        if (shouldStartBackgroundService(repository)) {
            startBackgroundService()
        }

        navigateInitialScreen(savedInstanceState, repository)
        registerBackPressedHandler()
    }

    private fun preloadResourceStores() {

        // Tải store trên IO thread để launcher vào màn hình chính sớm hơn.
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                StringResStore.load(this@MainActivity)
                ThemeColorStore.load(this@MainActivity)
            }
        }
    }

    private fun shouldStartBackgroundService(repository: PreferenceRepository): Boolean {

        val hasFilePermission = PermissionManager.hasFilePermission()
        val isFileCleanupEnabled = repository.isFileCleanupEnabled()
        val hasUsageStatsPermission = PermissionManager.hasUsageStatsPermission()
        val hasOverlayPermission = PermissionManager.hasOverlayPermission()
        val isAppBlockEnabled = repository.isAppBlockEnabled()
        val isEmergencyCallEnabled = repository.isEmergencyCallEnabled()

        Log.d(
            TAG,
            "BackgroundService | hasFilePermission=$hasFilePermission | " +
                "isFileCleanupEnabled=$isFileCleanupEnabled | " +
                "hasUsageStatsPermission=$hasUsageStatsPermission | " +
                "hasOverlayPermission=$hasOverlayPermission | " +
                "isAppBlockEnabled=$isAppBlockEnabled | " +
                "isEmergencyCallEnabled=$isEmergencyCallEnabled"
        )

        val canRunFileCleanup = hasFilePermission && isFileCleanupEnabled
        val canRunAppBlock = hasUsageStatsPermission && hasOverlayPermission && isAppBlockEnabled

        return canRunFileCleanup || canRunAppBlock || isEmergencyCallEnabled
    }

    private fun navigateInitialScreen(
        savedInstanceState: Bundle?,
        repository: PreferenceRepository
    ) {

        if (savedInstanceState != null) {
            Log.d(TAG, "navigate | skipped | restoring from savedInstanceState")
            return
        }

        val deeplink = resolveInitialDeeplink(repository)
        Log.d(TAG, "navigate | deeplink=$deeplink")

        // Đợi root layout xong để fragment transaction không đụng race condition lúc onCreate.
        binding.root.doOnLayout {
            lifecycleScope.launch {
                sendDeeplink(deeplink)
            }
        }
    }

    private fun resolveInitialDeeplink(repository: PreferenceRepository): String {

        val isOnboardingCompleted = repository.isOnboardingCompleted()
        val isHomeIntent = intent.hasCategory(Intent.CATEGORY_HOME)

        return when {
            !isOnboardingCompleted -> DeepLinks.ONBOARDING
            isHomeIntent -> DeepLinks.HOME
            else -> DeepLinks.SETTINGS
        }.also { deeplink ->
            Log.d(
                TAG,
                "resolveInitialDeeplink | isOnboardingCompleted=$isOnboardingCompleted | " +
                    "isHomeIntent=$isHomeIntent | deeplink=$deeplink"
            )
        }
    }

    private fun registerBackPressedHandler() {

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                when {
                    shouldPopBackStack() -> supportFragmentManager.popBackStack()
                    shouldReturnToHome() -> sendDeeplink(DeepLinks.HOME)
                    else -> finish()
                }
            }
        })
    }

    private fun shouldPopBackStack(): Boolean {

        val backStackCount = supportFragmentManager.backStackEntryCount
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        Log.d(
            TAG,
            "onBackPressed | backStackCount=$backStackCount | " +
                "currentFragment=${currentFragment?.javaClass?.simpleName}"
        )

        return backStackCount > 0
    }

    private fun shouldReturnToHome(): Boolean {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        return intent.hasCategory(Intent.CATEGORY_HOME) && currentFragment !is HomeFragment
    }

    override fun onNewIntent(intent: Intent) {

        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent | action=${intent.action} | categories=${intent.categories}")

        // Home intent cần đẩy user về launcher screen thay vì giữ màn hình cũ.
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

        logCurrentFragmentOnResume()
        val repository = PreferenceRepository.instance
        if (shouldStartBackgroundService(repository)) {
            startBackgroundService()
        }
    }

    private fun logCurrentFragmentOnResume() {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        Log.d(TAG, "onResume | currentFragment=${currentFragment?.javaClass?.simpleName}")
    }

    companion object {

        private const val TAG = "tuanha"
    }
}
