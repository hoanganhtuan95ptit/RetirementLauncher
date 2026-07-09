package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.PreferenceRepository

class CheckPinUseCase(private val repository: PreferenceRepository) {

    operator fun invoke(inputPin: String): Boolean {

        return repository.getPin() == inputPin
    }

    companion object {

        val instance: CheckPinUseCase by lazy { CheckPinUseCase(PreferenceRepository.instance) }
    }
}
