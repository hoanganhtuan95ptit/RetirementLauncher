package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetHomeAppsUseCase(private val repository: AppRepository) {

    fun asFlow(): Flow<List<HomeContentEntity>> = repository.homeDataFlow()
        .map { invoke() }
        .flowOn(Dispatchers.IO)

    operator fun invoke(): List<HomeContentEntity> {
        val allApps = repository.getInstalledApps()
        val selectedPackages = repository.getSelectedPackages()
        
        val apps = if (selectedPackages.isEmpty()) {
            allApps.map { HomeContentEntity.App(it) }
        } else {
            allApps.filter { selectedPackages.contains(it.packageName) }
                .map { HomeContentEntity.App(it) }
        }.toMutableList()

        val currentApp = repository.getCurrentApp()
        if (apps.none { it.entity.packageName == currentApp.packageName }) {
            apps.add(HomeContentEntity.App(currentApp))
        }

        apps.sortBy { it.entity.label.lowercase() }

        val contacts = repository.getSelectedContacts()
            .map { HomeContentEntity.Contact(it) }
            .sortedBy { it.entity.name.lowercase() }

        return apps + contacts
    }

    companion object {
        val instance: GetHomeAppsUseCase by lazy { GetHomeAppsUseCase(AppRepository.instance) }
    }
}
