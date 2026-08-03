package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import kotlinx.coroutines.flow.first

class GetSelectableAppsUseCase(private val repository: AppRepository) {

    operator suspend fun invoke(): List<SelectableAppEntity> {

        val allApps = repository.getAllAppFlow().first()
        val selectedPackages = repository.getSelectedPackagesFlow().first()

        return allApps.map { app ->
            SelectableAppEntity(app, selectedPackages.contains(app.packageName))
        }
    }

    companion object {

        val instance: GetSelectableAppsUseCase by lazy { GetSelectableAppsUseCase(AppRepository.instance) }
    }
}
