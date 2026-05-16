package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.MemoryRepositoryImpl
import com.simple.launcher.retirement.domain.model.RamInfo
import kotlinx.coroutines.flow.Flow

/**
 * Quản lý bộ nhớ khả dụng / có thể giải phóng.
 */
interface MemoryRepository {
    /** Đọc thông tin RAM hiện tại (total, used, free) từ ActivityManager. */
    fun getRamInfo(): RamInfo

    /** Ước tính số byte có thể giải phóng (allocatable - current free). */
    fun estimateCleanableMemory(): Long

    /** Flow phát giá trị MB có thể giải phóng mỗi khi được trigger. */
    fun estimateCleanableMemoryMBFlow(): Flow<Long>

    /** Thực sự yêu cầu hệ thống giải phóng bộ nhớ, trả về số byte đã giải phóng. */
    fun cleanMemory(): Long

    fun refreshMemoryStatus()

    companion object {
        val instance: MemoryRepository by lazy { MemoryRepositoryImpl(MainApplication.instance) }
    }
}
