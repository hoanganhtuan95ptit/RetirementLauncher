package com.simple.launcher.retirement.data.repository

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceRepositoryImpl(context: Context) : PreferenceRepository {

    private val sharedPrefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private var isPendingDefaultLauncherRam: Boolean = false
    private var pendingEmergencyConfigRam: com.simple.launcher.retirement.domain.model.SOSConfig? = null
    private var pendingAppBlockEnabledRam: Boolean? = null
    private var pendingFileCleanupEnabledRam: Boolean? = null
    private var pendingCallBlockEnabledRam: Boolean? = null

    companion object {

        private const val KEY_PIN = "app_pin"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_APP_BLOCK_ENABLED = "app_block_enabled"
        private const val KEY_APP_BLOCK_FIRST_TIME = "app_block_first_time"
        private const val KEY_FILE_CLEANUP_ENABLED = "file_cleanup_enabled"
        private const val KEY_FILE_CLEANUP_FIRST_TIME = "file_cleanup_first_time"
        private const val KEY_CALL_BLOCK_ENABLED = "call_block_enabled"
        private const val KEY_CALL_BLOCK_FIRST_TIME = "call_block_first_time"
        private const val KEY_POCKET_MODE_ENABLED = "pocket_mode_enabled"
        private const val KEY_EMERGENCY_CALL_ENABLED = "emergency_call_enabled"
        private const val KEY_EMERGENCY_CALL_FIRST_TIME = "emergency_call_first_time"
        private const val KEY_EMERGENCY_PHONE_NUMBER = "emergency_phone_number"
        private const val KEY_LAST_USER_ACTIVITY = "last_user_activity"
        private const val KEY_LAST_EMERGENCY_INDEX = "last_emergency_index"
        private const val KEY_EMERGENCY_TIMEOUT = "emergency_timeout"
        private const val KEY_EXCLUSION_PERIODS = "exclusion_periods"

        private const val DEFAULT_EMERGENCY_TIMEOUT = 10 * 60 * 60 * 1000L // 10h
    }

    private val _appBlockEnabled = MutableStateFlow(isAppBlockEnabled())
    private val _fileCleanupEnabled = MutableStateFlow(isFileCleanupEnabled())
    private val _callBlockEnabled = MutableStateFlow(isCallBlockEnabled())
    private val _pocketModeEnabled = MutableStateFlow(isPocketModeEnabled())
    private val _emergencyCallEnabled = MutableStateFlow(isEmergencyCallEnabled())
    private val _hasPin = MutableStateFlow(hasPin())

    // PIN
    override fun hasPin(): Boolean = sharedPrefs.contains(KEY_PIN)

    override fun hasPinFlow(): Flow<Boolean> = _hasPin.asStateFlow()

    override fun getPin(): String? = sharedPrefs.getString(KEY_PIN, null)

    override fun setPin(pin: String) {

        sharedPrefs.edit { putString(KEY_PIN, pin) }
        _hasPin.value = true
    }

    // Onboarding
    override fun isOnboardingCompleted(): Boolean =
        sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted(completed: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, completed) }
    }

    // App Block
    override fun isAppBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_APP_BLOCK_ENABLED, false)

    override fun appBlockEnabledFlow(): Flow<Boolean> = _appBlockEnabled.asStateFlow()

    override fun setAppBlockEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_APP_BLOCK_ENABLED, enabled) }
        _appBlockEnabled.value = enabled
    }

    override fun isAppBlockFirstTime(): Boolean =
        sharedPrefs.getBoolean(KEY_APP_BLOCK_FIRST_TIME, true)

    override fun setAppBlockFirstTime(firstTime: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_APP_BLOCK_FIRST_TIME, firstTime) }
    }

    override fun getPendingAppBlockEnabled(): Boolean? = pendingAppBlockEnabledRam

    override fun setPendingAppBlockEnabled(enabled: Boolean?) {

        pendingAppBlockEnabledRam = enabled
    }

    // File Cleanup
    override fun isFileCleanupEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_FILE_CLEANUP_ENABLED, false)

    override fun fileCleanupEnabledFlow(): Flow<Boolean> = _fileCleanupEnabled.asStateFlow()

    override fun setFileCleanupEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_FILE_CLEANUP_ENABLED, enabled) }
        _fileCleanupEnabled.value = enabled
    }

    override fun isFileCleanupFirstTime(): Boolean =
        sharedPrefs.getBoolean(KEY_FILE_CLEANUP_FIRST_TIME, true)

    override fun setFileCleanupFirstTime(firstTime: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_FILE_CLEANUP_FIRST_TIME, firstTime) }
    }

    override fun getPendingFileCleanupEnabled(): Boolean? = pendingFileCleanupEnabledRam

    override fun setPendingFileCleanupEnabled(enabled: Boolean?) {

        pendingFileCleanupEnabledRam = enabled
    }

    // Call Block
    override fun isCallBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_CALL_BLOCK_ENABLED, false)

    override fun callBlockEnabledFlow(): Flow<Boolean> = _callBlockEnabled.asStateFlow()

    override fun setCallBlockEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_CALL_BLOCK_ENABLED, enabled) }
        _callBlockEnabled.value = enabled
    }

    override fun isCallBlockFirstTime(): Boolean =
        sharedPrefs.getBoolean(KEY_CALL_BLOCK_FIRST_TIME, true)

    override fun setCallBlockFirstTime(firstTime: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_CALL_BLOCK_FIRST_TIME, firstTime) }
    }

    override fun getPendingCallBlockEnabled(): Boolean? = pendingCallBlockEnabledRam

    override fun setPendingCallBlockEnabled(enabled: Boolean?) {

        pendingCallBlockEnabledRam = enabled
    }

    // Pocket Mode
    override fun isPocketModeEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_POCKET_MODE_ENABLED, false)

    override fun pocketModeEnabledFlow(): Flow<Boolean> = _pocketModeEnabled.asStateFlow()

    override fun setPocketModeEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_POCKET_MODE_ENABLED, enabled) }
        _pocketModeEnabled.value = enabled
    }

    // Emergency Call
    override fun isEmergencyCallEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_EMERGENCY_CALL_ENABLED, false)

    override fun emergencyCallEnabledFlow(): Flow<Boolean> = _emergencyCallEnabled.asStateFlow()

    override fun setEmergencyCallEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_EMERGENCY_CALL_ENABLED, enabled) }
        _emergencyCallEnabled.value = enabled
    }

    override fun isEmergencyCallFirstTime(): Boolean =
        sharedPrefs.getBoolean(KEY_EMERGENCY_CALL_FIRST_TIME, true)

    override fun setEmergencyCallFirstTime(firstTime: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_EMERGENCY_CALL_FIRST_TIME, firstTime) }
    }

    override fun getEmergencyPhoneNumber(): String =
        sharedPrefs.getString(KEY_EMERGENCY_PHONE_NUMBER, "") ?: ""

    override fun setEmergencyPhoneNumber(number: String) {

        sharedPrefs.edit { putString(KEY_EMERGENCY_PHONE_NUMBER, number) }
    }

    override fun getLastEmergencyIndex(): Int =
        sharedPrefs.getInt(KEY_LAST_EMERGENCY_INDEX, -1)

    override fun setLastEmergencyIndex(index: Int) {

        sharedPrefs.edit { putInt(KEY_LAST_EMERGENCY_INDEX, index) }
    }

    override fun getEmergencyTimeout(): Long =
        sharedPrefs.getLong(KEY_EMERGENCY_TIMEOUT, DEFAULT_EMERGENCY_TIMEOUT)

    override fun setEmergencyTimeout(timeout: Long) {

        sharedPrefs.edit { putLong(KEY_EMERGENCY_TIMEOUT, timeout) }
    }

    override fun getExclusionPeriods(): List<ExclusionPeriod> {

        val json = sharedPrefs.getString(KEY_EXCLUSION_PERIODS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ExclusionPeriod>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun setExclusionPeriods(periods: List<ExclusionPeriod>) {

        val json = gson.toJson(periods)
        sharedPrefs.edit { putString(KEY_EXCLUSION_PERIODS, json) }
    }

    override fun getPendingEmergencyConfig(): com.simple.launcher.retirement.domain.model.SOSConfig? = pendingEmergencyConfigRam

    override fun setPendingEmergencyConfig(config: com.simple.launcher.retirement.domain.model.SOSConfig?) {

        pendingEmergencyConfigRam = config
    }

    override fun isPendingDefaultLauncher(): Boolean = isPendingDefaultLauncherRam

    override fun setPendingDefaultLauncher(pending: Boolean) {

        isPendingDefaultLauncherRam = pending
    }

    // User Activity
    override fun getLastUserActivity(): Long =
        sharedPrefs.getLong(KEY_LAST_USER_ACTIVITY, System.currentTimeMillis())

    override fun setLastUserActivity(timestamp: Long) {

        sharedPrefs.edit { putLong(KEY_LAST_USER_ACTIVITY, timestamp) }
    }
}
