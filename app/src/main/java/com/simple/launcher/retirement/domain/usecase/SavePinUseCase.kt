package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.AppRepository

class SavePinUseCase(private val repository: AppRepository) {
    operator fun invoke(pin: String) {
        repository.savePin(pin)
    }

    companion object {
        val instance: SavePinUseCase by lazy { SavePinUseCase(AppRepository.instance) }
    }
}
