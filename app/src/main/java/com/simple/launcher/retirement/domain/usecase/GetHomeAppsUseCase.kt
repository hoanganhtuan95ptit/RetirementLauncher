package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class GetHomeAppsUseCase(
    private val appRepository: AppRepository,
    private val contactRepository: ContactRepository
) {

    fun asFlow(): Flow<List<HomeContentEntity>> =
        merge(appRepository.homeDataFlow(), contactRepository.homeDataFlow())
            .map { invoke() }
            .flowOn(Dispatchers.IO)

    operator fun invoke(): List<HomeContentEntity> {
        val allApps = appRepository.getInstalledApps()
        val selectedPackages = appRepository.getSelectedPackages()

        val apps = if (selectedPackages.isEmpty()) {
            allApps.map { HomeContentEntity.App(it) }
        } else {
            allApps.filter { selectedPackages.contains(it.packageName) }
                .map { HomeContentEntity.App(it) }
        }.toMutableList()

        val currentApp = appRepository.getCurrentApp()
        if (apps.none { it.entity.packageName == currentApp.packageName }) {
            apps.add(HomeContentEntity.App(currentApp))
        }

        apps.sortBy { it.entity.label.lowercase() }

        val contacts = contactRepository.getSelectedContacts()
            .map { HomeContentEntity.Contact(it) }
            .sortedBy { it.entity.name.lowercase() }

        return apps + contacts
    }

    companion object {
        val instance: GetHomeAppsUseCase by lazy {
            GetHomeAppsUseCase(AppRepository.instance, ContactRepository.instance)
        }
    }
}
