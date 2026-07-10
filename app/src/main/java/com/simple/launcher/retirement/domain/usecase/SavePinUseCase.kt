package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.PreferenceRepository

class SavePinUseCase(private val repository: PreferenceRepository) {

    operator fun invoke(pin: String) {

        repository.setPin(pin)
    }

    companion object {

        val instance: SavePinUseCase by lazy { SavePinUseCase(PreferenceRepository.instance) }
    }
}
