package com.simple.launcher.retirement.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Telephony
import android.telecom.TelecomManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import java.io.File

class AppRepositoryImpl(private val context: Context) : AppRepository {

    private val sharedPrefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    // Trigger để các flow system status phát lại giá trị mới
    // replay = 1 đảm bảo subscriber mới nhận ngay giá trị gần nhất
    private val _systemTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    // Trigger để home data (app + contact) phát lại khi có thay đổi
    private val _dataTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    override fun countStrangeFilesFlow(): Flow<Int> = _systemTrigger
        .map { countStrangeFiles() }
        .flowOn(Dispatchers.IO)

    override fun estimateCleanableMemoryMBFlow(): Flow<Long> = _systemTrigger
        .map { estimateCleanableMemory() / (1024 * 1024) }
        .flowOn(Dispatchers.IO)

    override fun refreshSystemStatus() {
        _systemTrigger.tryEmit(Unit)
    }

    override fun homeDataFlow(): Flow<Unit> = _dataTrigger
    private val KEY_SELECTED_APPS = "selected_apps"
    private val KEY_PIN = "app_pin"
    private val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private val KEY_SELECTED_CONTACTS = "selected_contacts"
    private val KEY_APP_BLOCK_ENABLED = "app_block_enabled"
    private val KEY_FILE_CLEANUP_ENABLED = "file_cleanup_enabled"
    private val KEY_CALL_BLOCK_ENABLED = "call_block_enabled"

    private val gson = Gson()

    override fun getInstalledApps(): List<AppEntity> {
        val pm = context.packageManager
        val apps = mutableListOf<AppEntity>()
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val allApps = pm.queryIntentActivities(i, 0)
        for (ri in allApps) {
            val app = AppEntity(
                ri.loadLabel(pm).toString(),
                ri.activityInfo.packageName,
                ri.activityInfo.loadIcon(pm)
            )
            apps.add(app)
        }
        return apps.sortedBy { it.label.lowercase() }
    }

    override fun getCurrentApp(): AppEntity {
        val pm = context.packageManager
        val info = context.applicationInfo
        return AppEntity(
            label = info.loadLabel(pm).toString(),
            packageName = context.packageName,
            icon = info.loadIcon(pm)
        )
    }

    override fun getSelectedPackages(): Set<String> {
        return sharedPrefs.getStringSet(KEY_SELECTED_APPS, null) ?: emptySet()
    }

    override fun saveSelectedPackages(packages: Set<String>) {
        sharedPrefs.edit().putStringSet(KEY_SELECTED_APPS, packages).apply()
        _dataTrigger.tryEmit(Unit)
    }

    override fun getSelectedContacts(): List<ContactEntity> {
        val json = sharedPrefs.getString(KEY_SELECTED_CONTACTS, null)
        val contacts = if (json != null) {
            val type = object : TypeToken<List<ContactEntity>>() {}.type
            gson.fromJson<List<ContactEntity>>(json, type)
        } else {
            emptyList()
        }

        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (contacts.isEmpty() && isDebug) {
            return listOf(
                ContactEntity("1", "Con gái", "0123456789"),
                ContactEntity("2", "Con trai", "0987654321"),
                ContactEntity("3", "Bác sĩ", "0112233445")
            )
        }
        return contacts
    }

    override fun saveSelectedContacts(contacts: List<ContactEntity>) {
        val json = gson.toJson(contacts)
        sharedPrefs.edit().putString(KEY_SELECTED_CONTACTS, json).apply()
        _dataTrigger.tryEmit(Unit)
    }

    override fun getPin(): String? {
        return sharedPrefs.getString(KEY_PIN, null)
    }

    override fun savePin(pin: String) {
        sharedPrefs.edit().putString(KEY_PIN, pin).apply()
    }

    override fun hasPin(): Boolean {
        return sharedPrefs.contains(KEY_PIN)
    }

    override fun isOnboardingCompleted(): Boolean {
        return sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    override fun isAppBlockEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_APP_BLOCK_ENABLED, true)
    }

    override fun setAppBlockEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_APP_BLOCK_ENABLED, enabled).apply()
    }

    override fun isFileCleanupEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_FILE_CLEANUP_ENABLED, true)
    }

    override fun setFileCleanupEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_FILE_CLEANUP_ENABLED, enabled).apply()
    }

    override fun isCallBlockEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_CALL_BLOCK_ENABLED, false)
    }

    override fun setCallBlockEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_CALL_BLOCK_ENABLED, enabled).apply()
    }

    override fun isDefaultApp(packageName: String): Boolean {
        val pm = context.packageManager

        // Default Launcher
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveInfo?.activityInfo?.packageName == packageName) return true

        // Default Dialer
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        if (telecomManager?.defaultDialerPackage == packageName) return true

        // Default SMS
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)
        if (defaultSms == packageName) return true

        return false
    }

    override fun scanAndDeleteUnwantedFiles() {
        val root = Environment.getExternalStorageDirectory()
        findAndDeleteApkFiles(root)
    }

    override fun countStrangeFiles(): Int {
        val root = Environment.getExternalStorageDirectory()
        return recursiveCountStrange(root)
    }

    private fun recursiveCountStrange(file: File): Int {
        var count = 0
        if (file.isDirectory) {
            if (file.name.startsWith(".") || file.name == "Android") return 0
            file.listFiles()?.forEach { child ->
                count += recursiveCountStrange(child)
            }
        } else {
            if (isStrangeFile(file)) {
                count++
            }
        }
        return count
    }

    override fun estimateCleanableMemory(): Long {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return 0L
        }

        val storageManager = context.getSystemService(StorageManager::class.java)
        val uuid = storageManager.getUuidForPath(context.filesDir)

        val currentFree = context.filesDir.usableSpace
        val allocatable = storageManager.getAllocatableBytes(uuid)

        return maxOf(0L, allocatable - currentFree)
    }

    override fun deleteStrangeFiles() {
        val directoriesToScan = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStorageDirectory()
        )

        directoriesToScan.forEach { dir ->
            dir?.let { recursiveDeleteStrange(it) }
        }
    }

    override fun cleanMemory(): Long {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return 0L
        }

        val storageManager = context.getSystemService(StorageManager::class.java)

        val uuid = storageManager.getUuidForPath(context.filesDir)
        val freeableBytes = estimateCleanableMemory()

        if (freeableBytes <= 0) {
            return 0L
        }

        storageManager.allocateBytes(uuid, freeableBytes)

        return freeableBytes
    }

    private fun recursiveDeleteStrange(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                recursiveDeleteStrange(child)
            }
        } else {
            if (isStrangeFile(file)) {
                try {
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isStrangeFile(file: File): Boolean {
        val name = file.name.lowercase()
        val allowedExtensions = setOf(
            // Documents
            "doc", "docx", "txt", "pdf", "xls", "xlsx", "ppt", "pptx",
            // Images
            "jpg", "jpeg", "png", "gif", "webp", "bmp",
            // Audio
            "mp3", "wav", "m4a", "ogg", "flac",
            // Video
            "mp4", "mkv", "avi", "mov", "3gp"
        )

        val extension = name.substringAfterLast('.', "")
        if (extension.isEmpty()) return true // File không có đuôi cũng coi là lạ

        return !allowedExtensions.contains(extension)
    }

    private fun findAndDeleteApkFiles(file: File) {
        if (file.isDirectory) {
            // Bỏ qua các thư mục ẩn hoặc hệ thống để tối ưu
            if (file.name.startsWith(".") || file.name == "Android") return

            file.listFiles()?.forEach { child ->
                findAndDeleteApkFiles(child)
            }
        } else {
            val name = file.name.lowercase()
            if (name.endsWith(".apk") || name.endsWith(".aab")) {
                try {
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
