package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.FileRepositoryImpl
import kotlinx.coroutines.flow.Flow

/**
 * Quản lý việc quét / xóa file lạ trên external storage.
 */
interface FileRepository {
    fun scanAndDeleteUnwantedFiles()
    fun deleteStrangeFiles()

    /** Xóa tất cả file lạ trong một category cụ thể, trả về (số file, bytes đã xóa). */
    fun deleteStrangeFilesByCategory(category: StrangeFileCategory): Pair<Int, Long>

    fun countStrangeFiles(): Int
    fun countStrangeFilesFlow(): Flow<Int>
    fun refreshFileStatus()

    companion object {
        val instance: FileRepository by lazy { FileRepositoryImpl(MainApplication.instance) }
    }
}
