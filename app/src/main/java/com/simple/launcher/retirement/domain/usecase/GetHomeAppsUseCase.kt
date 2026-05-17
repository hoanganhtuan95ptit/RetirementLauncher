package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn

class GetHomeAppsUseCase(
    private val appRepository: AppRepository,
    private val contactRepository: ContactRepository
) {
    // Scope riêng cho usecase — tồn tại suốt vòng đời của singleton instance
    private val usecaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // shareIn đảm bảo chỉ có MỘT upstream flow chạy, dù có nhiều subscriber (appHomeItems, contactHomeItems).
    // Replay = 1 để subscriber mới nhận ngay giá trị gần nhất.
    // Trước đây: mỗi subscriber gọi asFlow() riêng → invoke() chạy 2 lần mỗi trigger.
    private val sharedFlow: Flow<List<HomeContentEntity>> =
        merge(appRepository.homeDataFlow(), contactRepository.homeDataFlow())
            .map { invoke() }
            .flowOn(Dispatchers.IO)
            .shareIn(usecaseScope, started = SharingStarted.WhileSubscribed(5_000), replay = 1)

    fun asFlow(): Flow<List<HomeContentEntity>> = sharedFlow

    operator fun invoke(): List<HomeContentEntity> {
        val allApps = appRepository.getInstalledApps()
        val selectedPackages = appRepository.getSelectedPackages()

        val apps = if (selectedPackages.isEmpty()) {
            allApps.map { HomeContentEntity.App(it) }.sortedBy { it.entity.label.lowercase() }.toMutableList()
        } else {
            selectedPackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
                .map { HomeContentEntity.App(it) }.toMutableList()
        }

        val currentApp = appRepository.getCurrentApp()
        if (apps.none { it.entity.packageName == currentApp.packageName }) {
            apps.add(HomeContentEntity.App(currentApp))
        }

        val contacts = contactRepository.getSelectedContacts()
            .map { HomeContentEntity.Contact(it) }

        return apps + contacts
    }

    companion object {
        val instance: GetHomeAppsUseCase by lazy {
            GetHomeAppsUseCase(AppRepository.instance, ContactRepository.instance)
        }
    }
}
