package com.simple.launcher.retirement.presentation.clock.configs

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.exts.colorAccent
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.exts.withStyleBodyMedium
import com.simple.launcher.retirement.utils.exts.withStyleBodySmall
import com.simple.launcher.retirement.utils.exts.withStyleTitleLarge
import com.simple.launcher.retirement.utils.exts.withStyleTitleMedium
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClockSettingBottomSheetViewModel : BaseViewModel() {

    private val preferenceRepository = PreferenceRepository.instance

    val horizontalPadding: StateFlow<Int> = combineState(resources, 24.dp()) {

        value = 24.dp()
    }

    val verticalPadding: StateFlow<Int> = combineState(resources, 24.dp()) {

        value = 24.dp()
    }

    val title: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.setting_clock_title)
            .withStyleTitleLarge()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val formatHeader: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.setting_clock_format_header)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val twelveHourTitle: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.setting_clock_12h)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val twelveHourExample: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorSecondary
        value = resources.getString(R.string.setting_clock_12h_example)
            .withStyleBodySmall()
            .with(BigForegroundColor(color))
            .build()
    }

    val twentyFourHourTitle: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.setting_clock_24h)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val twentyFourHourExample: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorSecondary
        value = resources.getString(R.string.setting_clock_24h_example)
            .withStyleBodySmall()
            .with(BigForegroundColor(color))
            .build()
    }

    val amPmTitle: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.setting_clock_am_pm)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val amPmDesc: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorSecondary
        value = resources.getString(R.string.setting_clock_am_pm_desc)
            .withStyleBodySmall()
            .with(BigForegroundColor(color))
            .build()
    }

    val solarTitle: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.setting_clock_solar)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val solarDesc: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorSecondary
        value = resources.getString(R.string.setting_clock_solar_desc)
            .withStyleBodySmall()
            .with(BigForegroundColor(color))
            .build()
    }

    val lunarTitle: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.setting_clock_lunar)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val lunarDesc: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.textColorSecondary
        value = resources.getString(R.string.setting_clock_lunar_desc)
            .withStyleBodySmall()
            .with(BigForegroundColor(color))
            .build()
    }

    val cancelLabel: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = resources.colorPrimary
        value = resources.getString(R.string.back)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val saveLabel: StateFlow<BigText> = combineState(resources, BigText("")) { resources ->

        val color = android.graphics.Color.WHITE
        value = resources.getString(R.string.save)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color), BigBold)
            .build()
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
