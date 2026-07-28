package com.simple.launcher.retirement.presentation.clock.configs

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.toBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClockSettingBottomSheetViewModel : BaseViewModel() {

    private val preferenceRepository = PreferenceRepository.instance

    val title = combineState(resources, "".toBuilder().build()) { resources ->
        value = resources.getString(R.string.setting_clock_title).toBuilder().build()
    }

    private val _is24HourFormat = MutableStateFlow(preferenceRepository.is24HourFormat())
    val is24HourFormat = _is24HourFormat.asStateFlow()

    private val _isAmPmEnabled = MutableStateFlow(preferenceRepository.isAmPmEnabled())
    val isAmPmEnabled = _isAmPmEnabled.asStateFlow()

    private val _isSolarCalendarEnabled = MutableStateFlow(preferenceRepository.isSolarCalendarEnabled())
    val isSolarCalendarEnabled = _isSolarCalendarEnabled.asStateFlow()

    private val _isLunarCalendarEnabled = MutableStateFlow(preferenceRepository.isLunarCalendarEnabled())
    val isLunarCalendarEnabled = _isLunarCalendarEnabled.asStateFlow()

    fun toggle24HourFormat(is24Hour: Boolean) {
        _is24HourFormat.value = is24Hour
        if (is24Hour) {
            _isAmPmEnabled.value = false
        }
    }

    fun toggleAmPmEnabled(enabled: Boolean) {
        _isAmPmEnabled.value = enabled
    }

    fun toggleSolarCalendarEnabled(enabled: Boolean) {
        _isSolarCalendarEnabled.value = enabled
    }

    fun toggleLunarCalendarEnabled(enabled: Boolean) {
        _isLunarCalendarEnabled.value = enabled
    }

    fun onSave() {
        preferenceRepository.set24HourFormat(_is24HourFormat.value)
        preferenceRepository.setAmPmEnabled(_isAmPmEnabled.value)
        preferenceRepository.setSolarCalendarEnabled(_isSolarCalendarEnabled.value)
        preferenceRepository.setLunarCalendarEnabled(_isLunarCalendarEnabled.value)
    }
}
