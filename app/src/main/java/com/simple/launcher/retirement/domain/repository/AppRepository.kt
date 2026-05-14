package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.model.ContactEntity
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getInstalledApps(): List<AppEntity>
    fun getCurrentApp(): AppEntity
    fun getSelectedPackages(): Set<String>
    fun saveSelectedPackages(packages: Set<String>)
    fun getSelectedContacts(): List<ContactEntity>
    fun saveSelectedContacts(contacts: List<ContactEntity>)
    fun getPin(): String?
    fun savePin(pin: String)
    fun hasPin(): Boolean
    fun scanAndDeleteUnwantedFiles()
    fun deleteStrangeFiles()
    fun cleanMemory(): Long
    fun countStrangeFiles(): Int
    fun estimateCleanableMemory(): Long
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
    fun isAppBlockEnabled(): Boolean
    fun setAppBlockEnabled(enabled: Boolean)
    fun isFileCleanupEnabled(): Boolean
    fun setFileCleanupEnabled(enabled: Boolean)
    fun isCallBlockEnabled(): Boolean
    fun setCallBlockEnabled(enabled: Boolean)
    fun isDefaultApp(packageName: String): Boolean

    // Flow API — mỗi nguồn data tự phát giá trị mới khi được trigger
    fun countStrangeFilesFlow(): Flow<Int>
    fun estimateCleanableMemoryMBFlow(): Flow<Long>
    fun refreshSystemStatus()

    // Flow phát lại khi danh sách app / contact thay đổi
    fun homeDataFlow(): Flow<Unit>

    companion object {
        val instance: AppRepository by lazy { AppRepositoryImpl(MainApplication.instance) }
    }
}
