package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.AppRepository

class CleanStorageUseCase(private val repository: AppRepository) {
    operator fun invoke() {
        repository.scanAndDeleteUnwantedFiles()
    }
}
