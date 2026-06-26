package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.PreferenceRepositoryImpl
import kotlinx.coroutines.flow.Flow

/**
 * Quản lý tất cả preference / cài đặt người dùng:
 * PIN, onboarding, app-block, file-cleanup, call-block.
 */
interface PreferenceRepository {
    fun getPin(): String?
    fun savePin(pin: String)
    fun hasPin(): Boolean
    fun hasPinFlow(): Flow<Boolean>

    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)

    fun isAppBlockEnabled(): Boolean
    fun setAppBlockEnabled(enabled: Boolean)
    fun isAppBlockEnabledFlow(): Flow<Boolean>

    fun isFileCleanupEnabled(): Boolean
    fun setFileCleanupEnabled(enabled: Boolean)
    fun isFileCleanupEnabledFlow(): Flow<Boolean>

    fun isCallBlockEnabled(): Boolean
    fun setCallBlockEnabled(enabled: Boolean)
    fun isCallBlockEnabledFlow(): Flow<Boolean>

    fun isPocketModeEnabled(): Boolean
    fun setPocketModeEnabled(enabled: Boolean)
    fun isPocketModeEnabledFlow(): Flow<Boolean>

    fun isEmergencyCallEnabled(): Boolean
    fun setEmergencyCallEnabled(enabled: Boolean)
    fun isEmergencyCallEnabledFlow(): Flow<Boolean>

    fun getEmergencyPhoneNumber(): String
    fun setEmergencyPhoneNumber(number: String)

    fun getLastUserActivity(): Long
    fun setLastUserActivity(timestamp: Long)

    fun getLastEmergencyIndex(): Int
    fun setLastEmergencyIndex(index: Int)

    companion object {
        val instance: PreferenceRepository by lazy { PreferenceRepositoryImpl(MainApplication.instance) }
    }
}
