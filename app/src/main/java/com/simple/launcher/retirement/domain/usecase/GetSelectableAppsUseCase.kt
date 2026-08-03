package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class GetSelectableAppsUseCase(private val repository: AppRepository) {

    operator fun invoke(): Flow<List<SelectableAppEntity>> = combine(
        repository.getAllAppFlow(),
        repository.getSelectedPackagesFlow()
    ) { allApps, selectedPackages ->

        allApps.map { app ->
            SelectableAppEntity(app, selectedPackages.contains(app.packageName))
        }
    }.flowOn(Dispatchers.IO)

    companion object {

        val instance: GetSelectableAppsUseCase by lazy { GetSelectableAppsUseCase(AppRepository.instance) }
    }
}
