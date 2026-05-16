package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.FileRepository

class CleanStorageUseCase(private val repository: FileRepository) {
    operator fun invoke() {
        repository.scanAndDeleteUnwantedFiles()
    }

    companion object {
        val instance: CleanStorageUseCase by lazy { CleanStorageUseCase(FileRepository.instance) }
    }
}
