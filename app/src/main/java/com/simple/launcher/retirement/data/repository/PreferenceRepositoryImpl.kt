package com.simple.launcher.retirement.data.repository

import android.util.Log
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken
import com.simple.launcher.retirement.data.AppPrefs
import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import com.simple.launcher.retirement.domain.model.SOSConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class PreferenceRepositoryImpl : PreferenceRepository {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private val sharedPrefs = AppPrefs.sharedPrefs
    private val gson = AppPrefs.gson

    // RAM-only flags — không persist qua process kill.
    private var isPendingDefaultLauncherRam: Boolean = false
    private var pendingEmergencyConfigRam: SOSConfig? = null
    private var pendingAppBlockEnabledRam: Boolean? = null
    private var pendingCallBlockEnabledRam: Boolean? = null
    private var pendingNotificationBlockEnabledRam: Boolean? = null

    // ── 2. Flows ──────────────────────────────────────────────────────────
    // Load lại preference khi flow active, và update trực tiếp sau mỗi lần save.

    private val _appBlockEnabled = mutableStateFlow<Boolean?>(null) {

        value = isAppBlockEnabled()
    }
    private val _callBlockEnabled = mutableStateFlow<Boolean?>(null) {

        value = isCallBlockEnabled()
    }
    private val _pocketModeEnabled = mutableStateFlow<Boolean?>(null) {

        value = isPocketModeEnabled()
    }
    private val _emergencyCallEnabled = mutableStateFlow<Boolean?>(null) {

        value = isEmergencyCallEnabled()
    }
    private val _lunarCalendarEnabled = mutableStateFlow<Boolean?>(null) {

        value = isLunarCalendarEnabled()
    }
    private val _is24HourFormat = mutableStateFlow<Boolean?>(null) {

        value = is24HourFormat()
    }
    private val _isAmPmEnabled = mutableStateFlow<Boolean?>(null) {

        value = isAmPmEnabled()
    }
    private val _isSolarCalendarEnabled = mutableStateFlow<Boolean?>(null) {

        value = isSolarCalendarEnabled()
    }
    private val _hasPin = mutableStateFlow<Boolean?>(null) {

        value = hasPin()
    }
    private val _notificationBlockEnabled = mutableStateFlow<Boolean?>(null) {

        value = isNotificationBlockEnabled()
    }
    private val _notificationBlockedPackages = mutableStateFlow<Set<String>?>(null) {

        value = getNotificationBlockedPackages()
    }
    private val _notificationRetentionMillis = mutableStateFlow<Long?>(null) {

        value = getNotificationRetentionMillis()
    }

    // ── 3. Public API ─────────────────────────────────────────────────────

    // ── PIN ──
    override fun hasPin(): Boolean = sharedPrefs.contains(KEY_PIN).apply {

        println("tuanha: $this getPin:${getPin()}---")
    }

    override fun hasPinFlow(): Flow<Boolean> = _hasPin.filterNotNull()

    override fun getPin(): String? = sharedPrefs.getString(KEY_PIN, null)

    override fun setPin(pin: String) {

        sharedPrefs.edit { putString(KEY_PIN, pin) }
        _hasPin.value = true
    }

    // ── Onboarding ──
    override fun isOnboardingCompleted(): Boolean =
        sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted(completed: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, completed) }
    }

    // ── App Block ──
    override fun isAppBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_APP_BLOCK_ENABLED, false)

    override fun appBlockEnabledFlow(): Flow<Boolean> =
        _appBlockEnabled.filterNotNull()

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

    // ── Call Block ──
    override fun isCallBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_CALL_BLOCK_ENABLED, false)

    override fun callBlockEnabledFlow(): Flow<Boolean> =
        _callBlockEnabled.filterNotNull()

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

    // ── Pocket Mode ──
    override fun isPocketModeEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_POCKET_MODE_ENABLED, false)

    override fun pocketModeEnabledFlow(): Flow<Boolean> =
        _pocketModeEnabled.filterNotNull()

    override fun setPocketModeEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_POCKET_MODE_ENABLED, enabled) }
        _pocketModeEnabled.value = enabled
    }

    // ── Emergency Call ──
    override fun isEmergencyCallEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_EMERGENCY_CALL_ENABLED, false)

    override fun emergencyCallEnabledFlow(): Flow<Boolean> =
        _emergencyCallEnabled.filterNotNull()

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

    override fun getSosCallAttemptCount(): Int =
        sharedPrefs.getInt(KEY_SOS_CALL_ATTEMPT_COUNT, 0)

    override fun setSosCallAttemptCount(count: Int) {

        sharedPrefs.edit { putInt(KEY_SOS_CALL_ATTEMPT_COUNT, count) }
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

    override fun getPendingEmergencyConfig(): SOSConfig? = pendingEmergencyConfigRam

    override fun setPendingEmergencyConfig(config: SOSConfig?) {

        pendingEmergencyConfigRam = config
    }

    override fun isPendingDefaultLauncher(): Boolean = isPendingDefaultLauncherRam

    override fun setPendingDefaultLauncher(pending: Boolean) {

        isPendingDefaultLauncherRam = pending
    }

    // ── User Activity ──
    // Trả về giá trị ổn định khi key chưa tồn tại — dùng thời điểm khởi tạo repository
    // (~ app process start) thay vì `System.currentTimeMillis()` mỗi call. Nếu dùng
    // `currentTimeMillis()` làm default, mỗi lần call sẽ trả giá trị khác nhau
    // → elapsed luôn ~ 0 → SOS không bao giờ trigger cho tới khi có setLastUserActivity đầu tiên.
    override fun getLastUserActivity(): Long {

        val stored = sharedPrefs.getLong(KEY_LAST_USER_ACTIVITY, Long.MIN_VALUE)
        if (stored != Long.MIN_VALUE) return stored

        // Seed lần đầu: coi như user vừa hoạt động ngay lúc install/enable SOS.
        val seed = System.currentTimeMillis()
        sharedPrefs.edit { putLong(KEY_LAST_USER_ACTIVITY, seed) }
        return seed
    }

    override fun setLastUserActivity(timestamp: Long) {

        sharedPrefs.edit { putLong(KEY_LAST_USER_ACTIVITY, timestamp) }
    }

    // ── Lunar Calendar ──
    override fun isLunarCalendarEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_LUNAR_CALENDAR_ENABLED, false)

    override fun lunarCalendarEnabledFlow(): Flow<Boolean> =
        _lunarCalendarEnabled.filterNotNull()

    override fun setLunarCalendarEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_LUNAR_CALENDAR_ENABLED, enabled) }
        _lunarCalendarEnabled.value = enabled
    }

    // ── Clock Format ──
    override fun is24HourFormat(): Boolean =
        sharedPrefs.getBoolean(KEY_CLOCK_24H_FORMAT, false)

    override fun is24HourFormatFlow(): Flow<Boolean> =
        _is24HourFormat.filterNotNull()

    override fun set24HourFormat(is24Hour: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_CLOCK_24H_FORMAT, is24Hour) }
        _is24HourFormat.value = is24Hour
    }

    override fun isAmPmEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_CLOCK_AM_PM_ENABLED, false)

    override fun isAmPmEnabledFlow(): Flow<Boolean> =
        _isAmPmEnabled.filterNotNull()

    override fun setAmPmEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_CLOCK_AM_PM_ENABLED, enabled) }
        _isAmPmEnabled.value = enabled
    }

    override fun isSolarCalendarEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_SOLAR_CALENDAR_ENABLED, true)

    override fun isSolarCalendarEnabledFlow(): Flow<Boolean> =
        _isSolarCalendarEnabled.filterNotNull()

    override fun setSolarCalendarEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_SOLAR_CALENDAR_ENABLED, enabled) }
        _isSolarCalendarEnabled.value = enabled
    }

    // ── Notification Block ──
    override fun isNotificationBlockEnabled(): Boolean =
        sharedPrefs.getBoolean(KEY_NOTIFICATION_BLOCK_ENABLED, false)

    override fun notificationBlockEnabledFlow(): Flow<Boolean> =
        _notificationBlockEnabled.filterNotNull()

    override fun setNotificationBlockEnabled(enabled: Boolean) {

        sharedPrefs.edit { putBoolean(KEY_NOTIFICATION_BLOCK_ENABLED, enabled) }
        _notificationBlockEnabled.value = enabled
    }

    override fun getNotificationBlockedPackages(): Set<String> {

        return sharedPrefs.getStringSet(KEY_NOTIFICATION_BLOCKED_PACKAGES, null)
            ?.toSet()
            ?: emptySet()
    }

    override fun notificationBlockedPackagesFlow(): Flow<Set<String>> =
        _notificationBlockedPackages.filterNotNull()

    override fun setNotificationBlockedPackages(packages: Set<String>) {

        sharedPrefs.edit { putStringSet(KEY_NOTIFICATION_BLOCKED_PACKAGES, packages) }
        _notificationBlockedPackages.value = packages
    }

    override fun getNotificationRetentionMillis(): Long =
        sharedPrefs.getLong(KEY_NOTIFICATION_RETENTION_MILLIS, 0L)

    override fun notificationRetentionMillisFlow(): Flow<Long> =
        _notificationRetentionMillis.filterNotNull()

    override fun setNotificationRetentionMillis(millis: Long) {

        sharedPrefs.edit { putLong(KEY_NOTIFICATION_RETENTION_MILLIS, millis) }
        _notificationRetentionMillis.value = millis
    }

    override fun getPendingNotificationBlockEnabled(): Boolean? = pendingNotificationBlockEnabledRam

    override fun setPendingNotificationBlockEnabled(enabled: Boolean?) {

        pendingNotificationBlockEnabledRam = enabled
    }

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        private const val KEY_PIN = "app_pin"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_APP_BLOCK_ENABLED = "app_block_enabled"
        private const val KEY_APP_BLOCK_FIRST_TIME = "app_block_first_time"
        private const val KEY_CALL_BLOCK_ENABLED = "call_block_enabled"
        private const val KEY_CALL_BLOCK_FIRST_TIME = "call_block_first_time"
        private const val KEY_POCKET_MODE_ENABLED = "pocket_mode_enabled"
        private const val KEY_EMERGENCY_CALL_ENABLED = "emergency_call_enabled"
        private const val KEY_EMERGENCY_CALL_FIRST_TIME = "emergency_call_first_time"
        private const val KEY_EMERGENCY_PHONE_NUMBER = "emergency_phone_number"
        private const val KEY_LAST_USER_ACTIVITY = "last_user_activity"
        private const val KEY_LAST_EMERGENCY_INDEX = "last_emergency_index"
        private const val KEY_SOS_CALL_ATTEMPT_COUNT = "sos_call_attempt_count"
        private const val KEY_LUNAR_CALENDAR_ENABLED = "lunar_calendar_enabled"
        private const val KEY_CLOCK_24H_FORMAT = "clock_24h_format"
        private const val KEY_CLOCK_AM_PM_ENABLED = "clock_am_pm_enabled"
        private const val KEY_SOLAR_CALENDAR_ENABLED = "solar_calendar_enabled"
        private const val KEY_EMERGENCY_TIMEOUT = "emergency_timeout"
        private const val KEY_EXCLUSION_PERIODS = "exclusion_periods"
        private const val KEY_NOTIFICATION_BLOCK_ENABLED = "notification_block_enabled"
        private const val KEY_NOTIFICATION_BLOCKED_PACKAGES = "notification_blocked_packages"
        private const val KEY_NOTIFICATION_RETENTION_MILLIS = "notification_retention_millis"

        private const val DEFAULT_EMERGENCY_TIMEOUT = 10 * 60 * 60 * 1000L // 10h
    }
}
