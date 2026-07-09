package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import com.simple.launcher.retirement.domain.model.StorageInfo
import com.simple.launcher.retirement.domain.repository.MemoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class MemoryRepositoryImpl(private val context: Context) : MemoryRepository {

    override fun getStorageInfo(): StorageInfo {

        val stat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes
        val usedBytes = totalBytes - freeBytes
        return StorageInfo(
            totalMB = totalBytes / (1024 * 1024),
            usedMB = usedBytes / (1024 * 1024),
            freeMB = freeBytes / (1024 * 1024)
        )
    }

    // replay = 1 đảm bảo subscriber mới nhận ngay giá trị gần nhất
    private val _systemTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    override fun estimateCleanableMemoryMBFlow(): Flow<Long> = _systemTrigger
        .map { estimateCleanableMemory() / (1024 * 1024) }
        .flowOn(Dispatchers.IO)

    override fun refreshMemoryStatus() {

        _systemTrigger.tryEmit(Unit)
    }

    override fun estimateCleanableMemory(): Long {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return 0L

        val storageManager = context.getSystemService(StorageManager::class.java)
        val uuid = storageManager.getUuidForPath(context.filesDir)
        val currentFree = context.filesDir.usableSpace
        val allocatable = storageManager.getAllocatableBytes(uuid)
        return maxOf(0L, allocatable - currentFree)
    }

    override fun cleanMemory(): Long {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return 0L

        val storageManager = context.getSystemService(StorageManager::class.java)
        val uuid = storageManager.getUuidForPath(context.filesDir)
        val freeableBytes = estimateCleanableMemory()
        if (freeableBytes <= 0) return 0L

        storageManager.allocateBytes(uuid, freeableBytes)
        return freeableBytes
    }
}
