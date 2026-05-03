package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.repository.AppRepository

class HasPinUseCase(private val repository: AppRepository) {
    operator fun invoke(): Boolean = repository.hasPin()
}
