package com.simple.launcher.retirement.data.repository

import android.os.Build
import android.os.Environment
import android.os.FileObserver
import com.simple.launcher.retirement.data.repository.FileRepositoryImpl.ALLOWED_EXTENSIONS
import com.simple.launcher.retirement.data.repository.FileRepositoryImpl._fileChangeTrigger
import com.simple.launcher.retirement.data.repository.FileRepositoryImpl.isExcludedDirectory
import com.simple.launcher.retirement.data.repository.FileRepositoryImpl.notifyFileSystemChanged
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.domain.repository.StrangeFileCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object FileRepositoryImpl : FileRepository {

    /**
     * Danh sách đuôi file được phép tồn tại trên storage (ảnh / nhạc / video).
     * Mọi file có đuôi không nằm trong danh sách này đều bị coi là "file không xác định" (unrecognized).
     */
    private val ALLOWED_EXTENSIONS = setOf(
        // Ảnh
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif",
        // Nhạc
        "mp3", "wav", "m4a", "ogg", "flac", "aac",
        // Video
        "mp4", "mkv", "avi", "mov", "3gp", "webm"
    )

    /**
     * Các thư mục con của Android/ cần bỏ qua — chứa file hệ thống quan trọng,
     * không phải file của người dùng.
     *   - obb   : expansion files của app (game assets, v.v.)
     *   - media : media riêng của từng app (voicemail, ringtone, v.v.)
     */
    private val ANDROID_SKIP_DIRS = setOf("obb", "media")

    /**
     * Trigger nội bộ để broadcast sự kiện thay đổi hệ thống file.
     * replay = 1 đảm bảo subscriber mới nhận ngay giá trị gần nhất khi subscribe.
     */
    private val _fileChangeTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    // -------------------------------------------------------------------------
    // Public API — FileRepository
    // -------------------------------------------------------------------------

    /**
     * Cold flow phát ra mỗi [File] mới xuất hiện trên external storage.
     *
     * - Dùng [channelFlow] + [awaitClose] để tự động dừng [FileObserver] khi flow bị cancel.
     * - Đệ quy watch toàn bộ cây thư mục từ [Environment.getExternalStorageDirectory].
     * - Với thư mục mới xuất hiện trong lúc watching, tự động đăng ký observer.
     * - Kết hợp tốt với `flatMapLatest` bên ngoài để bật/tắt theo preference.
     */
    override fun watchFilesFlow(): Flow<File> = channelFlow {

        // Map từ đường dẫn tuyệt đối → FileObserver, tránh đăng ký trùng.
        val observers = ConcurrentHashMap<String, FileObserver>()

        /**
         * Xử lý một file vừa được phát hiện:
         * - Nếu là APK/AAB và tính năng dọn dẹp bật → xóa ngay, không emit.
         * - Nếu là file unrecognized còn tồn tại → emit lên flow và trigger đếm lại.
         */
        fun processDetectedFile(file: File) {
            val name = file.name.lowercase()
            val isInstallerFile = name.endsWith(".apk") || name.endsWith(".aab")

            if (isInstallerFile && PreferenceRepository.instance.isFileCleanupEnabled() && file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    notifyFileSystemChanged()
                    return
                }
            }

            // File unrecognized còn tồn tại → emit để UI hiển thị cảnh báo
            if (file.exists()) {
                trySend(file)
                notifyFileSystemChanged()
            }
        }

        /**
         * Factory tạo [FileObserver] tương thích cả API < 29 (nhận path String)
         * lẫn API ≥ 29 (nhận File object). Chỉ lắng nghe sự kiện CREATE và MOVED_TO.
         */
        @Suppress("DEPRECATION")
        fun buildFileObserver(
            directory: File,
            onEvent: (event: Int, path: String?) -> Unit
        ): FileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(directory, CREATE or MOVED_TO) {
                override fun onEvent(event: Int, path: String?) {
                    this@channelFlow.launch { onEvent(event, path) }
                }
            }
        } else {
            object : FileObserver(directory.absolutePath, CREATE or MOVED_TO) {
                override fun onEvent(event: Int, path: String?) {
                    this@channelFlow.launch { onEvent(event, path) }
                }
            }
        }

        /**
         * Đăng ký watch đệ quy cho [directory] và tất cả thư mục con.
         * Bỏ qua nếu thư mục không tồn tại, bị loại trừ, hoặc đã được watch.
         */
        fun startWatchingDirectory(directory: File) {
            if (!directory.exists()) return
            if (!directory.isDirectory) return
            if (isExcludedDirectory(directory)) return
            if (observers.containsKey(directory.absolutePath)) return

            val observer = buildFileObserver(directory) { event, path ->
                if (path.isNullOrEmpty()) return@buildFileObserver

                val file = File(directory, path)

                when (event and (FileObserver.CREATE or FileObserver.MOVED_TO)) {
                    FileObserver.CREATE, FileObserver.MOVED_TO -> {
                        // Thư mục mới → đệ quy watch
                        if (file.isDirectory) startWatchingDirectory(file)
                        // File unrecognized mới → xử lý
                        if (file.isFile && isUnrecognizedFile(file)) processDetectedFile(file)
                    }
                }
            }

            observer.startWatching()
            observers[directory.absolutePath] = observer

            // Xử lý các file/thư mục đã có sẵn trước khi observer bắt đầu
            directory.listFiles()?.forEach { child ->
                if (child.isDirectory) startWatchingDirectory(child)
                if (child.isFile && isUnrecognizedFile(child)) processDetectedFile(child)
            }
        }

        startWatchingDirectory(Environment.getExternalStorageDirectory())

        awaitClose {
            // Dừng tất cả observer khi flow bị cancel để giải phóng tài nguyên
            observers.values.forEach { it.stopWatching() }
            observers.clear()
        }

    }.flowOn(Dispatchers.IO)

    /**
     * Flow phát ra số lượng file unrecognized hiện tại trên storage.
     * Tự động cập nhật mỗi khi [notifyFileSystemChanged] được gọi.
     */
    override fun countStrangeFilesFlow(): Flow<Int> = _fileChangeTrigger
        .map { countAllUnrecognizedFiles() }
        .flowOn(Dispatchers.IO)

    /**
     * Phát tín hiệu broadcast để các flow đang lắng nghe đếm lại số file.
     * Gọi sau mỗi thao tác thêm / xóa file.
     */
    override fun refreshFileStatus() {
        notifyFileSystemChanged()
    }

    /** Đếm tổng số file unrecognized trên toàn bộ external storage. */
    override fun countStrangeFiles(): Int = countAllUnrecognizedFiles()

    /**
     * Xóa tất cả file thuộc [category] trên external storage.
     *
     * @return Pair(số file đã xóa, tổng bytes đã giải phóng)
     */
    override fun deleteStrangeFilesByCategory(category: StrangeFileCategory): Pair<Int, Long> {
        val root = Environment.getExternalStorageDirectory()
        return deleteFilesInCategoryFrom(root, category)
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Phát tín hiệu cho [_fileChangeTrigger] — dùng nội bộ thay vì gọi override. */
    private fun notifyFileSystemChanged() {
        _fileChangeTrigger.tryEmit(Unit)
    }

    /** Đếm đệ quy số file unrecognized bắt đầu từ [root]. */
    private fun countAllUnrecognizedFiles(): Int {
        val root = Environment.getExternalStorageDirectory()
        return countUnrecognizedFilesIn(root)
    }

    /**
     * Đệ quy đếm file unrecognized trong [file] (có thể là file hoặc thư mục).
     * Bỏ qua các thư mục bị loại trừ ([isExcludedDirectory]).
     */
    private fun countUnrecognizedFilesIn(file: File): Int {
        if (file.isDirectory) {
            if (isExcludedDirectory(file)) return 0
            return file.listFiles()?.sumOf { countUnrecognizedFilesIn(it) } ?: 0
        }
        return if (isUnrecognizedFile(file)) 1 else 0
    }

    /**
     * Đệ quy xóa các file thuộc [category] bắt đầu từ [root].
     *
     * @return Pair(số file đã xóa thành công, tổng bytes đã giải phóng)
     */
    private fun deleteFilesInCategoryFrom(root: File, category: StrangeFileCategory): Pair<Int, Long> {
        if (root.isDirectory) {
            if (isExcludedDirectory(root)) return Pair(0, 0L)
            var totalCount = 0
            var totalBytes = 0L
            root.listFiles()?.forEach { child ->
                val (count, bytes) = deleteFilesInCategoryFrom(child, category)
                totalCount += count
                totalBytes += bytes
            }
            return Pair(totalCount, totalBytes)
        }

        // Đây là file — kiểm tra có thuộc category không trước khi xóa
        if (!isFileMatchingCategory(root, category)) return Pair(0, 0L)
        val fileSize = root.length()
        return try {
            if (root.exists() && root.delete()) Pair(1, fileSize) else Pair(0, 0L)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(0, 0L)
        }
    }

    /**
     * Kiểm tra xem [file] có thuộc [category] cần xóa không.
     *
     * Logic:
     * 1. File có đuôi trong [ALLOWED_EXTENSIONS] (ảnh/nhạc/video) → không thuộc category nào → giữ lại.
     * 2. [StrangeFileCategory.SYSTEM_TEMP] : bắt các file không đuôi, hoặc không thuộc category nào khác.
     * 3. Category còn lại   : so khớp trực tiếp với [StrangeFileCategory.extensions].
     */
    private fun isFileMatchingCategory(file: File, category: StrangeFileCategory): Boolean {
        val ext = file.name.lowercase().substringAfterLast('.', "")

        // File ảnh/nhạc/video → luôn giữ lại, không xóa
        if (ext.isNotEmpty() && ALLOWED_EXTENSIONS.contains(ext)) return false

        return when (category) {
            StrangeFileCategory.SYSTEM_TEMP ->
                // Không đuôi, hoặc đuôi khớp category này, hoặc đuôi không thuộc category nào khác
                ext.isEmpty() || category.extensions.contains(ext) ||
                        StrangeFileCategory.entries.none { it != category && it.extensions.contains(ext) }

            else -> category.extensions.contains(ext)
        }
    }

    /**
     * Trả về `true` nếu file không có đuôi hoặc đuôi không nằm trong [ALLOWED_EXTENSIONS].
     * File không đuôi cũng bị coi là unrecognized.
     */
    private fun isUnrecognizedFile(file: File): Boolean {
        val ext = file.name.lowercase().substringAfterLast('.', "")
        return ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)
    }

    /**
     * Trả về `true` nếu [file] là thư mục cần bỏ qua khi quét:
     * - Thư mục ẩn (bắt đầu bằng `.`)
     * - Thư mục con của `Android/obb` hoặc `Android/media` (file hệ thống quan trọng)
     */
    private fun isExcludedDirectory(file: File): Boolean {
        if (file.name.startsWith(".")) return true
        val canonicalPath = file.canonicalPath
        return ANDROID_SKIP_DIRS.any { skipDir -> canonicalPath.contains("/Android/$skipDir") }
    }
}
