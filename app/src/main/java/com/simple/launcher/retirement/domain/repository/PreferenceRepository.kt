package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.PreferenceRepositoryImpl
import kotlinx.coroutines.flow.Flow

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

    // File Cleanup
    fun isFileCleanupEnabled(): Boolean
    fun fileCleanupEnabledFlow(): Flow<Boolean>
    fun setFileCleanupEnabled(enabled: Boolean)
    fun isFileCleanupFirstTime(): Boolean
    fun setFileCleanupFirstTime(firstTime: Boolean)

    // Call Block
    fun isCallBlockEnabled(): Boolean
    fun callBlockEnabledFlow(): Flow<Boolean>
    fun setCallBlockEnabled(enabled: Boolean)
    fun isCallBlockFirstTime(): Boolean
    fun setCallBlockFirstTime(firstTime: Boolean)

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

    fun getPendingEmergencyCallEnabled(): Boolean?
    fun setPendingEmergencyCallEnabled(enabled: Boolean?)

    fun isPendingDefaultLauncher(): Boolean
    fun setPendingDefaultLauncher(pending: Boolean)

    // User Activity
    fun getLastUserActivity(): Long
    fun setLastUserActivity(timestamp: Long)

    companion object {

        val instance: PreferenceRepository by lazy { PreferenceRepositoryImpl(MainApplication.instance) }
    }
}
