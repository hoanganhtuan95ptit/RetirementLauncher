package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.MemoryRepositoryImpl
import com.simple.launcher.retirement.domain.model.StorageInfo
import kotlinx.coroutines.flow.Flow

/**
 * Quản lý bộ nhớ lưu trữ (bộ nhớ cứng) của thiết bị.
 */
interface MemoryRepository {
    /** Đọc thông tin bộ nhớ lưu trữ hiện tại (total, used, free) từ StatFs. */
    fun getStorageInfo(): StorageInfo

    /** Ước tính số byte cache có thể giải phóng (allocatable - current free). */
    fun estimateCleanableMemory(): Long

    /** Flow phát giá trị MB cache có thể giải phóng mỗi khi được trigger. */
    fun estimateCleanableMemoryMBFlow(): Flow<Long>

    /** Yêu cầu hệ thống dọn cache bộ nhớ lưu trữ, trả về số byte đã giải phóng. */
    fun cleanMemory(): Long

    fun refreshMemoryStatus()

    companion object {
        val instance: MemoryRepository by lazy { MemoryRepositoryImpl(MainApplication.instance) }
    }
}
