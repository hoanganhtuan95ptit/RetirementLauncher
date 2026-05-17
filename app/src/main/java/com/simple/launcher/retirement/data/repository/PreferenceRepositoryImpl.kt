package com.simple.launcher.retirement.data.repository

import android.content.Context
import com.simple.launcher.retirement.domain.repository.PreferenceRepository

class PreferenceRepositoryImpl(context: Context) : PreferenceRepository {

    private val sharedPrefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_APPS = "selected_apps"
        private const val KEY_PIN = "app_pin"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_APP_BLOCK_ENABLED = "app_block_enabled"
        private const val KEY_FILE_CLEANUP_ENABLED = "file_cleanup_enabled"
        private const val KEY_CALL_BLOCK_ENABLED = "call_block_enabled"
    }

    override fun getPin(): String? = sharedPrefs.getString(KEY_PIN, null)

    override fun savePin(pin: String) {
        sharedPrefs.edit().putString(KEY_PIN, pin).apply()
    }

    override fun hasPin(): Boolean = sharedPrefs.contains(KEY_PIN)

    override fun isOnboardingCompleted(): Boolean =
        sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    override fun isAppBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_APP_BLOCK_ENABLED, false)

    override fun setAppBlockEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_APP_BLOCK_ENABLED, enabled).apply()
    }

    override fun isFileCleanupEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_FILE_CLEANUP_ENABLED, false)

    override fun setFileCleanupEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_FILE_CLEANUP_ENABLED, enabled).apply()
    }

    override fun isCallBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_CALL_BLOCK_ENABLED, false)

    override fun setCallBlockEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_CALL_BLOCK_ENABLED, enabled).apply()
    }
}
