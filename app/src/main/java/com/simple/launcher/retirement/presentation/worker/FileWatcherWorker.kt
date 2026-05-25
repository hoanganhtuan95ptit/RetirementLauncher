package com.simple.launcher.retirement.presentation.worker

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.data.repository.FileRepositoryImpl
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File

class FileWatcherWorker(context: Context) : BackgroundWorker(context) {

    private val TAG = "FileWatcherWorker"
    private val observers = mutableListOf<FileObserver>()
    private val observersLock = Any()
    private val fileRepository = FileRepository.instance

    override fun observeEnabled(): Flow<Boolean> = PreferenceRepository.instance.isFileCleanupEnabledFlow()

    override fun onStart() {
        if (synchronized(observersLock) { observers.isNotEmpty() }) return
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart")
        buildWatchDirs().forEach { dir ->
            if (dir.exists() && dir.isDirectory) registerObserverRecursive(dir)
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Watching ${synchronized(observersLock) { observers.size }} directories")
    }

    override fun onStop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStop")
        stopWatchingAll()
    }

    private fun buildWatchDirs(): List<File> {
        val root = Environment.getExternalStorageDirectory()
        return listOfNotNull(
            root,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            File(root, "Bluetooth"),
            File(root, "Zalo"),
            File(root, "WhatsApp/Media/WhatsApp Documents"),
            File(root, "Telegram"),
            File(root, "Android/data"),
        ).filter { it.exists() && it.isDirectory }
    }

    /** Đệ quy register observer cho dir và toàn bộ thư mục con có sẵn bên trong. */
    private fun registerObserverRecursive(dir: File) {
        registerObserver(dir)
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) registerObserverRecursive(child)
        }
    }

    private fun registerObserver(dir: File) {
        val mask = FileObserver.CREATE or FileObserver.MOVED_TO
        val observer = createObserver(dir, mask)
        observer.startWatching()
        synchronized(observersLock) { observers.add(observer) }
    }

    private fun createObserver(file: File, mask: Int): FileObserver {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(file, mask) {
                override fun onEvent(event: Int, childPath: String?) {
                    handleEvent(file.absolutePath, childPath)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(file.absolutePath, mask) {
                override fun onEvent(event: Int, childPath: String?) {
                    handleEvent(file.absolutePath, childPath)
                }
            }
        }
    }

    private fun handleEvent(parentPath: String, childPath: String?) {
        if (childPath == null) return
        // Dispatch sang IO để không block FileObserver thread — tránh delay các event tiếp theo
        scope?.launch(Dispatchers.IO) {
            val fullFile = File(parentPath, childPath)

            // Folder mới xuất hiện → đăng ký observer đệ quy để theo dõi file bên trong
            if (fullFile.isDirectory) {
                registerObserverRecursive(fullFile)
                return@launch
            }

            val name = childPath.lowercase()
            val isApkOrAab = name.endsWith(".apk") || name.endsWith(".aab")

            // APK/AAB: kiểm tra cấu hình tự động xóa
            if (isApkOrAab && PreferenceRepository.instance.isFileCleanupEnabled() && fullFile.exists()) {
                val deleted = fullFile.delete()
                if (BuildConfig.DEBUG) Log.d(TAG, "Auto-deleted: $childPath, success=$deleted")
                if (deleted) {
                    fileRepository.refreshFileStatus()
                    return@launch
                }
            }

            // File lạ còn tồn tại (kể cả APK không bị xóa): cập nhật số trên home
            if (isStrangeFile(name) && fullFile.exists()) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Strange file detected: $childPath")
                fileRepository.refreshFileStatus()
            }
        }
    }

    private fun isStrangeFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "")
        return ext.isEmpty() || ext !in FileRepositoryImpl.ALLOWED_EXTENSIONS
    }

    private fun stopWatchingAll() {
        synchronized(observersLock) {
            observers.forEach { it.stopWatching() }
            observers.clear()
        }
    }
}
