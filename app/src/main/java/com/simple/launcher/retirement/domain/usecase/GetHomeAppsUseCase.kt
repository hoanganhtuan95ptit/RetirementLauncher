package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetHomeAppsUseCase(
    private val appRepository: AppRepository,
) {

    fun invoke(): Flow<List<HomeContentEntity.App>> = appRepository.homeDataFlow().map {

        buildHomeApps()
    }.flowOn(Dispatchers.IO)

    private fun buildHomeApps(): List<HomeContentEntity.App> {

        val allApps = appRepository.getInstalledApps()
        val selectedPackages = appRepository.getSelectedPackages()
        val apps = createSelectedHomeApps(allApps, selectedPackages).toMutableList()

        appendCurrentAppIfMissing(apps)
        return apps
    }

    private fun createSelectedHomeApps(
        allApps: List<AppEntity>,
        selectedPackages: List<String>
    ): List<HomeContentEntity.App> {

        if (selectedPackages.isEmpty()) {
            return allApps
                .map { HomeContentEntity.App(it) }
                .sortedBy { it.entity.label.lowercase() }
        }

        return selectedPackages
            .mapNotNull { packageName -> allApps.find { it.packageName == packageName } }
            .map { HomeContentEntity.App(it) }
    }

    private fun appendCurrentAppIfMissing(apps: MutableList<HomeContentEntity.App>) {

        val currentApp = appRepository.getCurrentApp()
        val isCurrentAppShown = apps.any { it.entity.packageName == currentApp.packageName }
        if (isCurrentAppShown) return

        apps.add(HomeContentEntity.App(currentApp))
    }

    companion object {

        val instance: GetHomeAppsUseCase by lazy {
            GetHomeAppsUseCase(AppRepository.instance)
        }
    }
}
