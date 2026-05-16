package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.PreferenceRepository

class HasPinUseCase(private val repository: PreferenceRepository) {
    operator fun invoke(): Boolean = repository.hasPin()

    companion object {
        val instance: HasPinUseCase by lazy { HasPinUseCase(PreferenceRepository.instance) }
    }
}
