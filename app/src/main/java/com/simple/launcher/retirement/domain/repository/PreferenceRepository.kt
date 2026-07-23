package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.PreferenceRepositoryImpl
import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Quản lý tất cả preference / cài đặt người dùng:
 * PIN, onboarding, app-block, file-cleanup, call-block.
 */
interface PreferenceRepository {

    // PIN
    fun hasPin(): Boolean
    fun hasPinFlow(): Flow<Boolean>
    fun getPin(): String?
    fun setPin(pin: String)

    // Onboarding
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)

    // App Block
    fun isAppBlockEnabled(): Boolean
    fun appBlockEnabledFlow(): Flow<Boolean>
    fun setAppBlockEnabled(enabled: Boolean)
    fun isAppBlockFirstTime(): Boolean
    fun setAppBlockFirstTime(firstTime: Boolean)
    fun getPendingAppBlockEnabled(): Boolean?
    fun setPendingAppBlockEnabled(enabled: Boolean?)


    // Call Block
    fun isCallBlockEnabled(): Boolean
    fun callBlockEnabledFlow(): Flow<Boolean>
    fun setCallBlockEnabled(enabled: Boolean)
    fun isCallBlockFirstTime(): Boolean
    fun setCallBlockFirstTime(firstTime: Boolean)
    fun getPendingCallBlockEnabled(): Boolean?
    fun setPendingCallBlockEnabled(enabled: Boolean?)

    // Pocket Mode
    fun isPocketModeEnabled(): Boolean
    fun pocketModeEnabledFlow(): Flow<Boolean>
    fun setPocketModeEnabled(enabled: Boolean)

    // Emergency Call
    fun isEmergencyCallEnabled(): Boolean
    fun emergencyCallEnabledFlow(): Flow<Boolean>
    fun setEmergencyCallEnabled(enabled: Boolean)
    fun isEmergencyCallFirstTime(): Boolean
    fun setEmergencyCallFirstTime(firstTime: Boolean)
    fun getEmergencyPhoneNumber(): String
    fun setEmergencyPhoneNumber(number: String)
    fun getLastEmergencyIndex(): Int
    fun setLastEmergencyIndex(index: Int)

    fun getPendingEmergencyConfig(): com.simple.launcher.retirement.domain.model.SOSConfig?
    fun setPendingEmergencyConfig(config: com.simple.launcher.retirement.domain.model.SOSConfig?)


    fun getEmergencyTimeout(): Long
    fun setEmergencyTimeout(timeout: Long)

    fun getExclusionPeriods(): List<ExclusionPeriod>
    fun setExclusionPeriods(periods: List<ExclusionPeriod>)

    fun isPendingDefaultLauncher(): Boolean
    fun setPendingDefaultLauncher(pending: Boolean)

    // User Activity
    fun getLastUserActivity(): Long
    fun setLastUserActivity(timestamp: Long)

    // Lunar Calendar
    fun isLunarCalendarEnabled(): Boolean
    fun lunarCalendarEnabledFlow(): StateFlow<Boolean>
    fun setLunarCalendarEnabled(enabled: Boolean)

    // Clock Format
    fun is24HourFormat(): Boolean
    fun is24HourFormatFlow(): StateFlow<Boolean>
    fun set24HourFormat(is24Hour: Boolean)

    fun isAmPmEnabled(): Boolean
    fun isAmPmEnabledFlow(): StateFlow<Boolean>
    fun setAmPmEnabled(enabled: Boolean)

    fun isSolarCalendarEnabled(): Boolean
    fun isSolarCalendarEnabledFlow(): StateFlow<Boolean>
    fun setSolarCalendarEnabled(enabled: Boolean)

    companion object {

        val instance: PreferenceRepository by lazy { PreferenceRepositoryImpl(MainApplication.instance) }
    }
}
