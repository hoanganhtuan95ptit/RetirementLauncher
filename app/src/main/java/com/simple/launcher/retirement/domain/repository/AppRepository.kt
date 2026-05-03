package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.model.ContactEntity

interface AppRepository {
    fun getInstalledApps(): List<AppEntity>
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
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
    fun isAppBlockEnabled(): Boolean
    fun setAppBlockEnabled(enabled: Boolean)
    fun isFileCleanupEnabled(): Boolean
    fun setFileCleanupEnabled(enabled: Boolean)
}
