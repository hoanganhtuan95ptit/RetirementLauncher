package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.domain.model.AppEntity
import kotlinx.coroutines.flow.Flow

/**
 * Quản lý danh sách ứng dụng và lựa chọn ứng dụng được phép trên màn hình home.
 */
interface AppRepository {

    fun getInstalledApps(): List<AppEntity>
    fun getCurrentApp(): AppEntity
    fun getSelectedPackages(): List<String>
    fun saveSelectedPackages(packages: List<String>)
    fun isDefaultApp(packageName: String): Boolean

    // Flow phát lại khi danh sách app / contact thay đổi
    fun homeDataFlow(): Flow<Unit>

    companion object {

        val instance: AppRepository by lazy { AppRepositoryImpl(MainApplication.instance) }
    }
}
