package com.simple.launcher.retirement.presentation.pin

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.emptyText
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PinSetupViewModel : BaseViewModel() {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private var tempPin: String = ""

    // ── 2. Flows ──────────────────────────────────────────────────────────

    private val _state = MutableStateFlow<State>(State.ENTER_NEW_PIN)
    val state: StateFlow<State> = _state

    private val _actionRes = MutableStateFlow(R.string.back)

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->

        val color = resources.textColorPrimary
        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.setting_pin), color),
            backIcon = buildBackIcon(color)
        )
    }

    val instruction: StateFlow<BigText> = combineState(
        flow1 = resources,
        flow2 = _state,
        initialValue = emptyText()
    ) { resources, state ->

        val color = resources.textColorPrimary
        val resId = when (state) {

            State.ENTER_NEW_PIN -> R.string.pin_enter_new
            State.CONFIRM_NEW_PIN -> R.string.pin_confirm_new
            State.SUCCESS -> R.string.pin_enter_new
        }

        value = resources.getString(resId)
            .with(BigForegroundColor(color))
            .build()
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = resources,
        flow2 = _actionRes,
        initialValue = ActionState.empty()
    ) { resources, actionRes ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary

        value = buildActionState(
            text = resources.getString(actionRes),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    init {

        _state.value = State.ENTER_NEW_PIN
        _actionRes.value = R.string.onboarding_start
    }

    // ── 3. Public API ─────────────────────────────────────────────────────

    fun handlePinInput(pin: String) {

        if (pin.length != 6) {

            _error.value = R.string.pin_error_length
            return
        }

        _error.value = null

        when (_state.value) {

            State.ENTER_NEW_PIN -> onEnterNewPin(pin)
            State.CONFIRM_NEW_PIN -> onConfirmNewPin(pin)
            else -> Unit
        }
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun onEnterNewPin(pin: String) {

        tempPin = pin
        _state.value = State.CONFIRM_NEW_PIN
        _actionRes.value = R.string.save
    }

    private fun onConfirmNewPin(pin: String) {

        if (pin != tempPin) {

            _error.value = R.string.pin_error_confirm_mismatch
            return
        }

        SavePinUseCase.instance(pin)
        _state.value = State.SUCCESS
    }

    // ── 5. Nested classes ─────────────────────────────────────────────────

    enum class State {

        ENTER_NEW_PIN,
        CONFIRM_NEW_PIN,
        SUCCESS
    }
}
