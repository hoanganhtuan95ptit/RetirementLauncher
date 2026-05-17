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
