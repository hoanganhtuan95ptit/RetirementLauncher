package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.model.HomeItem
import com.simple.launcher.retirement.domain.repository.AppRepository

class GetHomeAppsUseCase(private val repository: AppRepository) {
    operator fun invoke(): List<HomeItem> {
        val allApps = repository.getInstalledApps()
        val selectedPackages = repository.getSelectedPackages()
        
        val apps = if (selectedPackages.isEmpty()) {
            allApps.map { HomeItem.App(it) }
        } else {
            allApps.filter { selectedPackages.contains(it.packageName) }
                .map { HomeItem.App(it) }
        }

        val contacts = repository.getSelectedContacts().map { HomeItem.Contact(it) }
        
        return apps + contacts
    }
}
