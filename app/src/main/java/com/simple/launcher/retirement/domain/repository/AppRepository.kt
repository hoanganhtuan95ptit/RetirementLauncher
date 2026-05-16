package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.domain.model.AppEntity
import kotlinx.coroutines.flow.Flow

/**
 * Phân loại file sẽ bị xóa.
 * Chỉ giữ lại ảnh / nhạc / video — mọi thứ còn lại đều bị coi là không cần thiết.
 */
enum class StrangeFileCategory(val extensions: Set<String>) {
    /** File tạm, log, cache hệ thống — và mọi file không có đuôi */
    SYSTEM_TEMP(setOf("tmp", "log", "bak", "dat", "db-journal", "db-shm", "db-wal")),
    /** File nén, archive */
    COMPRESSED(setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")),
    /** Tài liệu văn phòng (không cần thiết với người dùng phổ thông) */
    DOCUMENTS(setOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "csv", "rtf")),
    /** APK cài đặt, file dex/cache ứng dụng */
    APK_CACHE(setOf("apk", "aab", "dex", "odex", "vdex"))
}

/**
 * Quản lý danh sách ứng dụng và lựa chọn ứng dụng được phép trên màn hình home.
 */
interface AppRepository {
    fun getInstalledApps(): List<AppEntity>
    fun getCurrentApp(): AppEntity
    fun getSelectedPackages(): Set<String>
    fun saveSelectedPackages(packages: Set<String>)
    fun isDefaultApp(packageName: String): Boolean

    // Flow phát lại khi danh sách app / contact thay đổi
    fun homeDataFlow(): Flow<Unit>

    companion object {
        val instance: AppRepository by lazy { AppRepositoryImpl(MainApplication.instance) }
    }
}
