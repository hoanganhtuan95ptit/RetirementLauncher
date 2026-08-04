package com.simple.launcher.retirement.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ActivityMainBinding
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.services.BackgroundService
import com.simple.launcher.retirement.utils.exts.backPressedFlow
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>() {

    // ── 3. Public API ─────────────────────────────────────────────────────

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)

    override fun setupViews(savedInstanceState: Bundle?) {

        logDebug {

            "setupViews | savedInstanceState=${savedInstanceState != null} | " +
                    "action=${intent.action} | categories=${intent.categories}"
        }

        navigateInitialScreen()

        registerBackPressedHandler()
    }

    override fun onNewIntent(intent: Intent) {

        super.onNewIntent(intent)

        navigateInitialScreen()
    }

    fun startBackgroundService() {

        BackgroundService.start(this)
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun navigateInitialScreen() {

        val deeplink = resolveInitialDeeplink(PreferenceRepository.instance)

        sendDeeplink(deeplink)
    }

    private fun resolveInitialDeeplink(repository: PreferenceRepository): String {

        val isOnboardingCompleted = repository.isOnboardingCompleted()
        val isHomeIntent = intent.hasCategory(Intent.CATEGORY_HOME)

        val isDefaultLauncher = PermissionManager.isDefaultLauncher()
        if (isDefaultLauncher) {

            repository.setPendingDefaultLauncher(false)
        }

        return when {

            !isOnboardingCompleted -> DeepLinks.ONBOARDING
            isHomeIntent && repository.isPendingDefaultLauncher() && !isDefaultLauncher -> DeepLinks.SETTINGS
            isHomeIntent -> DeepLinks.HOME
            else -> DeepLinks.SETTINGS
        }.also { deeplink ->

            logDebug {

                "resolveInitialDeeplink | isOnboardingCompleted=$isOnboardingCompleted | " +
                        "isHomeIntent=$isHomeIntent | deeplink=$deeplink | " +
                        "isPendingDefaultLauncher=${repository.isPendingDefaultLauncher()}"
            }
        }
    }

    private fun registerBackPressedHandler() {

        lifecycleScope.launch {

            onBackPressedDispatcher.backPressedFlow(this@MainActivity).collect { dispatchBackPressed() }
        }
    }

    private fun dispatchBackPressed() {

        when {

            shouldPopBackStack() -> supportFragmentManager.popBackStack()
            shouldReturnToHome() -> sendDeeplink(DeepLinks.HOME)
            else -> finish()
        }
    }

    private fun shouldPopBackStack(): Boolean {

        val backStackCount = supportFragmentManager.backStackEntryCount

        logDebug {

            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            "onBackPressed | backStackCount=$backStackCount | " +
                    "currentFragment=${currentFragment?.javaClass?.simpleName}"
        }

        return backStackCount > 0
    }

    private fun shouldReturnToHome(): Boolean {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        return intent.hasCategory(Intent.CATEGORY_HOME) && currentFragment !is HomeFragment
    }

    private inline fun logDebug(message: () -> String) {

        if (BuildConfig.DEBUG) Log.d(TAG, message())
    }

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        private const val TAG = "MainActivity"
    }
}
