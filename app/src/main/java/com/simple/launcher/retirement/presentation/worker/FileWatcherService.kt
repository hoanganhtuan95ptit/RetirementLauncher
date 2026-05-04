package com.simple.launcher.retirement.presentation.worker

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import java.io.File
import java.util.*

class FileWatcherService : Service() {

    private val TAG = "FileWatcherService"
    private val observers = mutableMapOf<String, FileObserver>()
    private lateinit var repository: AppRepositoryImpl

    override fun onCreate() {
        super.onCreate()
        repository = AppRepositoryImpl(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!repository.isFileCleanupEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }
        startRecursiveWatching()
        return START_STICKY
    }

    private fun startRecursiveWatching() {
        // Dừng tất cả các observer cũ nếu có
        stopWatchingAll()
        
        val rootPath = Environment.getExternalStorageDirectory()
        registerObserverRecursive(rootPath)
        Log.d(TAG, "Started recursive watching from: ${rootPath.path}")
    }

    private fun registerObserverRecursive(file: File) {
        val path = file.absolutePath
        if (observers.containsKey(path)) return

        try {
            val observer = createObserver(file)
            observer.startWatching()
            observers[path] = observer

            // Đăng ký cho các thư mục con hiện có
            file.listFiles()?.forEach { child ->
                if (child.isDirectory && !child.name.startsWith(".")) {
                    registerObserverRecursive(child)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering observer for $path: ${e.message}")
        }
    }

    private fun createObserver(file: File): FileObserver {
        val path = file.absolutePath
        val mask = FileObserver.CREATE or FileObserver.MOVED_TO
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(file, mask) {
                override fun onEvent(event: Int, childPath: String?) {
                    handleEvent(path, childPath)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(path, mask) {
                override fun onEvent(event: Int, childPath: String?) {
                    handleEvent(path, childPath)
                }
            }
        }
    }

    private fun handleEvent(parentPath: String, childPath: String?) {
        if (childPath == null) return
        
        val fullFile = File(parentPath, childPath)
        
        // Nếu là thư mục mới được tạo, đăng ký theo dõi nó luôn
        if (fullFile.isDirectory) {
            registerObserverRecursive(fullFile)
            return
        }

        // Nếu là file cài đặt, thực hiện xóa
        val name = childPath.lowercase()
        if (name.endsWith(".apk") || name.endsWith(".aab")) {
            if (fullFile.exists()) {
                val deleted = fullFile.delete()
                Log.d(TAG, "Automatically deleted: $childPath, success: $deleted")
            }
        }

        // Gửi broadcast thông báo có sự thay đổi file để UI cập nhật
        val intent = Intent("com.simple.launcher.retirement.FILE_CHANGED")
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    private fun stopWatchingAll() {
        observers.values.forEach { it.stopWatching() }
        observers.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopWatchingAll()
        super.onDestroy()
    }
}
