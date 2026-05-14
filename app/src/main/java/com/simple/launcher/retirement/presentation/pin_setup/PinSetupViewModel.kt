package com.simple.launcher.retirement.presentation.pin_setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.domain.usecase.CheckPinUseCase
import com.simple.launcher.retirement.domain.usecase.HasPinUseCase
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase

class PinSetupViewModel(
    private val hasPinUseCase: HasPinUseCase,
    private val checkPinUseCase: CheckPinUseCase,
    private val savePinUseCase: SavePinUseCase
) : BaseViewModel() {

    enum class State {
        ENTER_OLD_PIN,
        ENTER_NEW_PIN,
        CONFIRM_NEW_PIN,
        SUCCESS
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var tempPin: String = ""

    init {
        if (hasPinUseCase()) {
            _state.value = State.ENTER_OLD_PIN
        } else {
            _state.value = State.ENTER_NEW_PIN
        }
    }

    fun handlePinInput(pin: String) {
        if (pin.length != 6) {
            _error.value = "Mã PIN phải có 6 chữ số"
            return
        }
        _error.value = null

        when (_state.value) {
            State.ENTER_OLD_PIN -> {
                if (checkPinUseCase(pin)) {
                    _state.value = State.ENTER_NEW_PIN
                } else {
                    _error.value = "Mã PIN cũ không chính xác"
                }
            }
            State.ENTER_NEW_PIN -> {
                tempPin = pin
                _state.value = State.CONFIRM_NEW_PIN
            }
            State.CONFIRM_NEW_PIN -> {
                if (pin == tempPin) {
                    savePinUseCase(pin)
                    _state.value = State.SUCCESS
                } else {
                    _error.value = "Mã PIN xác nhận không khớp"
                }
            }
            else -> {}
        }
    }
}
