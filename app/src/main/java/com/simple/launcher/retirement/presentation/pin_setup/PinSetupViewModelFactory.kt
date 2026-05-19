package com.simple.launcher.retirement.presentation.pin_setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase

class PinSetupViewModelFactory(
    private val savePinUseCase: SavePinUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PinSetupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PinSetupViewModel(savePinUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
