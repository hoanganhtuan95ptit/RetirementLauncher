package com.simple.launcher.retirement.data.repository

import android.app.ActivityManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

class AppRepositoryImpl(private val context: Context) : AppRepository {

    private val sharedPrefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
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
        val currentPackage = context.packageName
        for (ri in allApps) {
            if (ri.activityInfo.packageName == currentPackage) continue
            val app = AppEntity(
                ri.loadLabel(pm).toString(),
                ri.activityInfo.packageName,
                ri.activityInfo.loadIcon(pm)
            )
            apps.add(app)
        }
        return apps.sortedBy { it.label.lowercase() }
    }

    override fun getSelectedPackages(): Set<String> {
        return sharedPrefs.getStringSet(KEY_SELECTED_APPS, null) ?: emptySet()
    }

    override fun saveSelectedPackages(packages: Set<String>) {
        sharedPrefs.edit().putStringSet(KEY_SELECTED_APPS, packages).apply()
    }

    override fun getSelectedContacts(): List<ContactEntity> {
        val json = sharedPrefs.getString(KEY_SELECTED_CONTACTS, null) ?: return emptyList()
        val type = object : TypeToken<List<ContactEntity>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun saveSelectedContacts(contacts: List<ContactEntity>) {
        val json = gson.toJson(contacts)
        sharedPrefs.edit().putString(KEY_SELECTED_CONTACTS, json).apply()
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
        return getJunkFiles().sumOf { it.second }
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
        val junkFiles = getJunkFiles()
        var freedBytes = 0L

        for ((uri, size) in junkFiles) {
            try {
                val deleted = context.contentResolver.delete(uri, null, null)
                if (deleted > 0) freedBytes += size
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return freedBytes
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
    private fun getJunkFiles(): List<Pair<Uri, Long>> {
        val junkFiles = mutableListOf<Pair<Uri, Long>>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        val junkMimeTypes = listOf(
            "application/vnd.android.package-archive", // APK
            "application/zip",                         // ZIP
            "application/octet-stream"                 // file rác khác
        )
        val selection = junkMimeTypes.joinToString(" OR ") {
            "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
        }

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            junkMimeTypes.toTypedArray(),
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            while (cursor.moveToNext()) {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    cursor.getLong(idCol)
                )
                val size = cursor.getLong(sizeCol)
                junkFiles.add(Pair(uri, size))
            }
        }
        return junkFiles
    }
}
