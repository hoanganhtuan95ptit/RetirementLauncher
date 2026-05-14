package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository

class GetHomeAppsUseCase(private val repository: AppRepository) {
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
            apps.add(0, HomeContentEntity.App(currentApp))
        }

        val contacts = repository.getSelectedContacts().map { HomeContentEntity.Contact(it) }
        
        return apps + contacts
    }

    companion object {
        val instance: GetHomeAppsUseCase by lazy { GetHomeAppsUseCase(AppRepository.instance) }
    }
}
