package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.PreferenceRepositoryImpl

/**
 * Quản lý tất cả preference / cài đặt người dùng:
 * PIN, onboarding, app-block, file-cleanup, call-block.
 */
interface PreferenceRepository {
    fun getPin(): String?
    fun savePin(pin: String)
    fun hasPin(): Boolean

    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)

    fun isAppBlockEnabled(): Boolean
    fun setAppBlockEnabled(enabled: Boolean)

    fun isFileCleanupEnabled(): Boolean
    fun setFileCleanupEnabled(enabled: Boolean)

    fun isCallBlockEnabled(): Boolean
    fun setCallBlockEnabled(enabled: Boolean)

    companion object {
        val instance: PreferenceRepository by lazy { PreferenceRepositoryImpl(MainApplication.instance) }
    }
}
