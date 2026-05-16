package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.os.Environment
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.StrangeFileCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File

class FileRepositoryImpl(private val context: Context) : FileRepository {

    companion object {
        /**
         * Chỉ giữ lại ảnh / nhạc / video.
         * Mọi đuôi khác đều bị coi là "file lạ" và xóa.
         */
        private val ALLOWED_EXTENSIONS = setOf(
            // Ảnh
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif",
            // Nhạc
            "mp3", "wav", "m4a", "ogg", "flac", "aac",
            // Video
            "mp4", "mkv", "avi", "mov", "3gp", "webm"
        )
    }

    // replay = 1 đảm bảo subscriber mới nhận ngay giá trị gần nhất
    private val _systemTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    override fun countStrangeFilesFlow(): Flow<Int> = _systemTrigger
        .map { countStrangeFiles() }
        .flowOn(Dispatchers.IO)

    override fun refreshFileStatus() {
        _systemTrigger.tryEmit(Unit)
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
            if (isStrangeFile(file)) count++
        }
        return count
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

    override fun deleteStrangeFilesByCategory(category: StrangeFileCategory): Pair<Int, Long> {
        val roots = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStorageDirectory()
        )
        var count = 0
        var bytes = 0L
        roots.forEach { dir ->
            dir?.let {
                val result = recursiveDeleteByCategory(it, category)
                count += result.first
                bytes += result.second
            }
        }
        return Pair(count, bytes)
    }

    private fun recursiveDeleteByCategory(file: File, category: StrangeFileCategory): Pair<Int, Long> {
        if (file.isDirectory) {
            if (file.name.startsWith(".") || file.name == "Android") return Pair(0, 0L)
            var count = 0; var bytes = 0L
            file.listFiles()?.forEach { child ->
                val r = recursiveDeleteByCategory(child, category)
                count += r.first; bytes += r.second
            }
            return Pair(count, bytes)
        }
        if (!belongsToCategory(file, category)) return Pair(0, 0L)
        val size = file.length()
        return try {
            if (file.exists() && file.delete()) Pair(1, size) else Pair(0, 0L)
        } catch (e: Exception) {
            e.printStackTrace(); Pair(0, 0L)
        }
    }

    private fun belongsToCategory(file: File, category: StrangeFileCategory): Boolean {
        val ext = file.name.lowercase().substringAfterLast('.', "")
        if (ext.isNotEmpty() && ALLOWED_EXTENSIONS.contains(ext)) return false
        return when (category) {
            StrangeFileCategory.SYSTEM_TEMP ->
                ext.isEmpty() || category.extensions.contains(ext) ||
                StrangeFileCategory.values().none { it != category && it.extensions.contains(ext) }
            else -> category.extensions.contains(ext)
        }
    }

    private fun recursiveDeleteStrange(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { child -> recursiveDeleteStrange(child) }
        } else {
            if (isStrangeFile(file)) {
                try { if (file.exists()) file.delete() } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private fun isStrangeFile(file: File): Boolean {
        val ext = file.name.lowercase().substringAfterLast('.', "")
        return ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)
    }

    private fun findAndDeleteApkFiles(file: File) {
        if (file.isDirectory) {
            if (file.name.startsWith(".") || file.name == "Android") return
            file.listFiles()?.forEach { child -> findAndDeleteApkFiles(child) }
        } else {
            val name = file.name.lowercase()
            if (name.endsWith(".apk") || name.endsWith(".aab")) {
                try { if (file.exists()) file.delete() } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }
}
