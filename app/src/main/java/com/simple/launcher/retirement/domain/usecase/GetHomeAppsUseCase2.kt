package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetHomeAppsUseCase2(
    private val appRepository: AppRepository,
) {

    fun invoke(): Flow<List<HomeContentEntity.App>> = appRepository.homeDataFlow().map {

        val allApps = appRepository.getInstalledApps()
        val selectedPackages = appRepository.getSelectedPackages()

        val apps = if (selectedPackages.isEmpty()) {
            allApps.map { HomeContentEntity.App(it) }.sortedBy { it.entity.label.lowercase() }.toMutableList()
        } else {
            selectedPackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }.map { HomeContentEntity.App(it) }.toMutableList()
        }

        val currentApp = appRepository.getCurrentApp()
        if (apps.none { it.entity.packageName == currentApp.packageName }) {
            apps.add(HomeContentEntity.App(currentApp))
        }

        apps
    }.flowOn(Dispatchers.IO)


    companion object {
        val instance: GetHomeAppsUseCase2 by lazy {
            GetHomeAppsUseCase2(AppRepository.instance)
        }
    }
}