package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.AppRepository

class CheckPinUseCase(private val repository: AppRepository) {
    operator fun invoke(inputPin: String): Boolean {
        return repository.getPin() == inputPin
    }

    companion object {
        val instance: CheckPinUseCase by lazy { CheckPinUseCase(AppRepository.instance) }
    }
}
