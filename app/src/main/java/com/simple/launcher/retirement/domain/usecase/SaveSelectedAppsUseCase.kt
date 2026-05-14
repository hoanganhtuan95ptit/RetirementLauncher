package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.AppRepository

class SaveSelectedAppsUseCase(private val repository: AppRepository) {
    operator fun invoke(packages: Set<String>) {
        repository.saveSelectedPackages(packages)
    }

    companion object {
        val instance: SaveSelectedAppsUseCase by lazy { SaveSelectedAppsUseCase(AppRepository.instance) }
    }
}
