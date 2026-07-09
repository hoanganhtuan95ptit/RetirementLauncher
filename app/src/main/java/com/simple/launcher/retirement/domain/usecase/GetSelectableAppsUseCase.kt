package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.repository.AppRepository

class GetSelectableAppsUseCase(private val repository: AppRepository) {

    operator fun invoke(): List<SelectableAppEntity> {

        val allApps = repository.getInstalledApps()
        val selectedPackages = repository.getSelectedPackages()

        return allApps.map { app ->
            SelectableAppEntity(app, selectedPackages.contains(app.packageName))
        }
    }

    companion object {

        val instance: GetSelectableAppsUseCase by lazy { GetSelectableAppsUseCase(AppRepository.instance) }
    }
}
