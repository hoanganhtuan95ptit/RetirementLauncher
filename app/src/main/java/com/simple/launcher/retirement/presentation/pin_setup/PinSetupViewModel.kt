package com.simple.launcher.retirement.presentation.pin_setup

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.*
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PinSetupViewModel(
    private val savePinUseCase: SavePinUseCase
) : BaseViewModel() {

    enum class State {
        ENTER_NEW_PIN,
        CONFIRM_NEW_PIN,
        SUCCESS
    }

    private val _state = MutableStateFlow<State>(State.ENTER_NEW_PIN)
    val state: StateFlow<State> = _state

    private val _actionRes = MutableStateFlow(R.string.back)

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ToolbarState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(R.string.setting_pin), color),
            backIcon = buildBackIcon(color)
        )
    }

    val instruction: StateFlow<RichText> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = _state,
        initialValue = emptyText()
    ) { stringMap, themeMap, state ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val resId = when (state) {
            State.ENTER_NEW_PIN -> R.string.pin_enter_new
            State.CONFIRM_NEW_PIN -> R.string.pin_confirm_new
            State.SUCCESS -> R.string.pin_enter_new
        }
        stringMap.getString(resId)
            .with(ForegroundColor(color))
            .build()
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = _actionRes,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap, actionRes ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight, android.graphics.Color.LTGRAY)

        buildActionState(
            text = stringMap.getString(actionRes),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error

    private var tempPin: String = ""

    init {
        _state.value = State.ENTER_NEW_PIN
        _actionRes.value = R.string.onboarding_start
    }

    fun handlePinInput(pin: String) {
        if (pin.length != 6) {
            _error.value = R.string.pin_error_length
            return
        }
        _error.value = null

        when (_state.value) {
            State.ENTER_NEW_PIN -> {
                tempPin = pin
                _state.value = State.CONFIRM_NEW_PIN
                _actionRes.value = R.string.save
            }
            State.CONFIRM_NEW_PIN -> {
                if (pin == tempPin) {
                    savePinUseCase(pin)
                    _state.value = State.SUCCESS
                } else {
                    _error.value = R.string.pin_error_confirm_mismatch
                }
            }
            else -> {}
        }
    }
}
