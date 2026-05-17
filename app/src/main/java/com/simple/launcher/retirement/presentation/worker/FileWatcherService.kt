package com.simple.launcher.retirement.presentation.worker

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import java.io.File

class FileWatcherService : Service() {

    private val TAG = "FileWatcherService"
    private val observers = mutableListOf<FileObserver>()
    private lateinit var repository: PreferenceRepository

    override fun onCreate() {
        super.onCreate()
        repository = PreferenceRepository.instance
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!repository.isFileCleanupEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }
        startWatching()
        return START_STICKY
    }

    private fun startWatching() {
        stopWatchingAll()

        // Chỉ watch các thư mục có khả năng cao nhận APK/file lạ,
        // thay vì đệ quy toàn bộ external storage (có thể tạo hàng trăm FileObserver).
        val watchDirs = buildWatchDirs()
        watchDirs.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                registerObserver(dir)
            }
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Watching ${observers.size} directories: ${watchDirs.map { it.name }}")
    }

    /** Danh sách các thư mục cần theo dõi — giới hạn ở những nơi thường chứa APK/file lạ. */
    private fun buildWatchDirs(): List<File> {
        val root = Environment.getExternalStorageDirectory()
        return listOfNotNull(
            root,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            File(root, "WhatsApp/Media/WhatsApp Documents"),
            File(root, "Telegram")
        ).filter { it.exists() && it.isDirectory }
    }

    private fun registerObserver(dir: File) {
        val mask = FileObserver.CREATE or FileObserver.MOVED_TO
        val observer = createObserver(dir, mask)
        observer.startWatching()
        observers.add(observer)
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

        val name = childPath.lowercase()
        // Chỉ xử lý APK/AAB — không broadcast cho mọi file event
        if (!name.endsWith(".apk") && !name.endsWith(".aab")) return

        val fullFile = File(parentPath, childPath)
        if (fullFile.exists()) {
            val deleted = fullFile.delete()
            if (BuildConfig.DEBUG) Log.d(TAG, "Auto-deleted: $childPath, success=$deleted")
        }

        // Broadcast chỉ khi thực sự có thay đổi liên quan
        val intent = Intent("com.simple.launcher.retirement.FILE_CHANGED")
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    private fun stopWatchingAll() {
        observers.forEach { it.stopWatching() }
        observers.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopWatchingAll()
        super.onDestroy()
    }
}
