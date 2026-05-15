package com.simple.launcher.retirement.presentation.pin_setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.domain.usecase.CheckPinUseCase
import com.simple.launcher.retirement.domain.usecase.HasPinUseCase
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.TextSize
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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

    private val _actionRes = MutableStateFlow(R.string.back) // Sẽ update theo state

    val toolbar: StateFlow<ToolbarState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(R.string.setting_pin), color),
            backIcon = buildBackIcon(color)
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ToolbarState.empty())

    val action: StateFlow<ActionState> = combine(strings, themes, _actionRes) { stringMap, themeMap, actionRes ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: android.graphics.Color.LTGRAY

        val text = stringMap.getString(actionRes)
            .toRich()
            .with(ForegroundColor(color), TextSize(18), Bold)
            
        val background = Background(
            backgroundColor = backgroundColor,
            cornerRadius_TL = DP.DP_12,
            cornerRadius_TR = DP.DP_12,
            cornerRadius_BL = DP.DP_12,
            cornerRadius_BR = DP.DP_12
        )
            
        ActionState(text = text, background = background)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var tempPin: String = ""

    init {
        if (hasPinUseCase()) {
            _state.value = State.ENTER_OLD_PIN
            _actionRes.value = R.string.back // Hoặc "Xác nhận" tùy ý, ví dụ R.string.save
        } else {
            _state.value = State.ENTER_NEW_PIN
            _actionRes.value = R.string.onboarding_start // Hoặc text phù hợp
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
                    _actionRes.value = R.string.back // Cập nhật text nút nếu cần
                } else {
                    _error.value = "Mã PIN cũ không chính xác"
                }
            }
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
                    _error.value = "Mã PIN xác nhận không khớp"
                }
            }
            else -> {}
        }
    }
}
