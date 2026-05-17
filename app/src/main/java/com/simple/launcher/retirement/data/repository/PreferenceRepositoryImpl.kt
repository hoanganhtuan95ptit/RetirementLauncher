package com.simple.launcher.retirement.data.repository

import android.content.Context
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceRepositoryImpl(context: Context) : PreferenceRepository {

    private val sharedPrefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_APPS = "selected_apps"
        private const val KEY_PIN = "app_pin"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_APP_BLOCK_ENABLED = "app_block_enabled"
        private const val KEY_FILE_CLEANUP_ENABLED = "file_cleanup_enabled"
        private const val KEY_CALL_BLOCK_ENABLED = "call_block_enabled"
        private const val KEY_POCKET_MODE_ENABLED = "pocket_mode_enabled"
    }

    private val _appBlockEnabled = MutableStateFlow(isAppBlockEnabled())
    private val _fileCleanupEnabled = MutableStateFlow(isFileCleanupEnabled())
    private val _callBlockEnabled = MutableStateFlow(isCallBlockEnabled())
    private val _pocketModeEnabled = MutableStateFlow(isPocketModeEnabled())
    private val _hasPin = MutableStateFlow(hasPin())

    override fun getPin(): String? = sharedPrefs.getString(KEY_PIN, null)

    override fun savePin(pin: String) {
        sharedPrefs.edit().putString(KEY_PIN, pin).apply()
        _hasPin.value = true
    }

    override fun hasPin(): Boolean = sharedPrefs.contains(KEY_PIN)

    override fun hasPinFlow(): Flow<Boolean> = _hasPin.asStateFlow()

    override fun isOnboardingCompleted(): Boolean =
        sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    override fun isAppBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_APP_BLOCK_ENABLED, false)

    override fun setAppBlockEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_APP_BLOCK_ENABLED, enabled).apply()
        _appBlockEnabled.value = enabled
    }

    override fun isAppBlockEnabledFlow(): Flow<Boolean> = _appBlockEnabled.asStateFlow()

    override fun isFileCleanupEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_FILE_CLEANUP_ENABLED, false)

    override fun setFileCleanupEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_FILE_CLEANUP_ENABLED, enabled).apply()
        _fileCleanupEnabled.value = enabled
    }

    override fun isFileCleanupEnabledFlow(): Flow<Boolean> = _fileCleanupEnabled.asStateFlow()

    override fun isCallBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_CALL_BLOCK_ENABLED, false)

    override fun setCallBlockEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_CALL_BLOCK_ENABLED, enabled).apply()
        _callBlockEnabled.value = enabled
    }

    override fun isCallBlockEnabledFlow(): Flow<Boolean> = _callBlockEnabled.asStateFlow()

    override fun isPocketModeEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_POCKET_MODE_ENABLED, false)

    override fun setPocketModeEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_POCKET_MODE_ENABLED, enabled).apply()
        _pocketModeEnabled.value = enabled
    }

    override fun isPocketModeEnabledFlow(): Flow<Boolean> = _pocketModeEnabled.asStateFlow()
}
