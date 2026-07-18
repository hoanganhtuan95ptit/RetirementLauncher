package com.simple.launcher.retirement.presentation.emergency.setting

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import com.simple.launcher.retirement.domain.model.SOSConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.presentation.emergency.adapters.SOSCardViewItem
import com.simple.launcher.retirement.presentation.emergency.adapters.SOSHeaderViewItem
import com.simple.launcher.retirement.presentation.emergency.adapters.SOSSectionHeaderViewItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorOutline
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSecondaryContainer
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import com.simple.launcher.retirement.utils.text.withStyleBodyMedium
import com.simple.launcher.retirement.utils.text.withStyleTitleLarge
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SOSSettingsViewModel : BaseViewModel() {

    val timeout = MutableStateFlow(PreferenceRepository.instance.getEmergencyTimeout())

    val exclusionPeriods = MutableStateFlow(PreferenceRepository.instance.getExclusionPeriods())

    val isFeatureEnabledDraft = MutableStateFlow(PreferenceRepository.instance.isEmergencyCallEnabled())

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->

        val color = resources.textColorPrimary
        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.sos_settings_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val saveAction: StateFlow<ActionState> = combineState(
        resources,
        ActionState.empty()
    ) { resources ->

        value = buildActionState(
            text = resources.getString(R.string.sos_save_changes),
            textColor = resources.colorOnPrimary,
            backgroundColor = resources.colorPrimary,
            strokeWidth = 1.dp().toInt(),
            strokeColor = resources.colorOutline
        ).copy(
            image = BigImage(R.drawable.ic_save),
            imageShow = true
        )
    }

    val viewItemList: StateFlow<List<ViewItem>> = combineState(
        resources,
        isFeatureEnabledDraft,
        timeout,
        exclusionPeriods,
        emptyList()
    ) { resources, isEnabledDraft, timeout, periods ->

        val list = mutableListOf<ViewItem>()

        // SOS Header
        list += SOSHeaderViewItem(
            title = resources.getString(R.string.sos_master_toggle)
                .withStyleTitleLarge()
                .with(BigForegroundColor(resources.textColorPrimary))
                .build(),
            desc = resources.getString(R.string.emergency_call_intro_desc)
                .withStyleBodyMedium()
                .with(BigForegroundColor(resources.textColorPrimary))
                .build(),
            icon = BigImage(R.drawable.ic_sos_black_24dp),
            iconBackground = Background.Builder()
                .backgroundColor(resources.colorSecondaryContainer)
                .cornerRadius(40.dp().toInt())
                .build(),
            isEnabled = isEnabledDraft
        )

        // Timeout Section
        list += SOSSectionHeaderViewItem(
            title = resources.getString(R.string.sos_timeout_label)
                .withStyleTitleLarge()
                .with(BigForegroundColor(resources.textColorPrimary))
                .build(),
            icon = BigImage(R.drawable.ic_clock),
            isEnabled = isEnabledDraft
        )

        val hours = (timeout / (60 * 60 * 1000L)).toInt()
        list += SOSCardViewItem(
            id = ID_TIMEOUT,
            title = resources.getString(R.string.sos_timeout_value).format(hours)
                .withStyleBodyLarge()
                .with(BigForegroundColor(resources.textColorPrimary))
                .build(),
            icon = BigImage(R.drawable.ic_clock),
            isEnabled = isEnabledDraft
        )

        // Exclusion Periods Section
        list += SOSSectionHeaderViewItem(
            title = resources.getString(R.string.sos_exclusion_periods_header)
                .withStyleTitleLarge()
                .with(BigForegroundColor(resources.textColorPrimary))
                .build(),
            icon = BigImage(R.drawable.ic_bed),
            isEnabled = isEnabledDraft
        )

        periods.forEachIndexed { index, period ->

            val timeText = resources.getString(R.string.sos_exclusion_period_format)
                .format(period.startHour, period.startMinute, period.endHour, period.endMinute)

            list += SOSCardViewItem(
                id = ID_PERIOD_ITEM_BASE + index,
                title = timeText
                    .withStyleBodyLarge()
                    .with(BigForegroundColor(resources.textColorPrimary))
                    .build(),
                icon = BigImage(R.drawable.ic_bed),
                endIcon = BigImage(R.drawable.ic_clear),
                isEnabled = isEnabledDraft
            )
        }

        list += SOSCardViewItem(
            id = ID_ADD_PERIOD,
            title = resources.getString(R.string.sos_add_exclusion_period)
                .withStyleBodyLarge()
                .with(BigForegroundColor(resources.textColorPrimary))
                .build(),
            desc = resources.getString(R.string.sos_add_exclusion_period_desc)
                .withStyleBodyMedium()
                .with(BigForegroundColor(resources.textColorSecondary))
                .build(),
            icon = BigImage(R.drawable.ic_add),
            isEnabled = isEnabledDraft
        )

        value = list
    }

    fun toggleFeatureDraft() {

        isFeatureEnabledDraft.value = !isFeatureEnabledDraft.value
    }

    fun updateTimeout(hours: Int) {

        timeout.value = hours * 60 * 60 * 1000L
    }

    fun addExclusionPeriod(period: ExclusionPeriod) {

        exclusionPeriods.value += period
    }

    fun removeExclusionPeriod(id: String) {

        exclusionPeriods.value = exclusionPeriods.value.filter { it.id != id }
    }

    fun save(): SOSConfig {

        return SOSConfig(
            isEnabled = isFeatureEnabledDraft.value,
            timeout = timeout.value,
            exclusionPeriods = exclusionPeriods.value
        )
    }

    companion object {

        const val ID_TIMEOUT = 200
        const val ID_ADD_PERIOD = 201
        const val ID_PERIOD_ITEM_BASE = 1000
    }
}
