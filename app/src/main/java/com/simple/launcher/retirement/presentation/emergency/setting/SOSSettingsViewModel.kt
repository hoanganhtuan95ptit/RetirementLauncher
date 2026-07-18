package com.simple.launcher.retirement.presentation.emergency.setting

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.BuildConfig
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
import com.simple.ui.precompute.text.BigText
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

        value = buildViewItems(resources, isEnabledDraft, timeout, periods)
    }

    private fun buildViewItems(
        resources: Map<String, Any>,
        isEnabledDraft: Boolean,
        timeout: Long,
        periods: List<ExclusionPeriod>
    ): List<ViewItem> {

        return listOf(
            buildHeader(resources, isEnabledDraft),
            buildTimeoutHeader(resources, isEnabledDraft),
            buildTimeoutCard(resources, timeout, isEnabledDraft),
            buildExclusionHeader(resources, isEnabledDraft)
        ) + buildPeriodCards(resources, periods, isEnabledDraft) +
            buildAddPeriodCard(resources, isEnabledDraft)
    }

    private fun buildHeader(resources: Map<String, Any>, isEnabledDraft: Boolean): ViewItem {

        return SOSHeaderViewItem(
            title = resources.getString(R.string.sos_master_toggle).asTitleText(resources),
            desc = resources.getString(R.string.emergency_call_intro_desc).asPrimaryBodyMediumText(resources),
            icon = BigImage(R.drawable.ic_sos_black_24dp),
            iconBackground = Background.Builder()
                .backgroundColor(resources.colorSecondaryContainer)
                .cornerRadius(40.dp().toInt())
                .build(),
            isEnabled = isEnabledDraft
        )
    }

    private fun buildTimeoutHeader(resources: Map<String, Any>, isEnabledDraft: Boolean): ViewItem {

        return SOSSectionHeaderViewItem(
            title = resources.getString(R.string.sos_timeout_label).asTitleText(resources),
            icon = BigImage(R.drawable.ic_clock),
            isEnabled = isEnabledDraft
        )
    }

    private fun buildTimeoutCard(
        resources: Map<String, Any>,
        timeout: Long,
        isEnabledDraft: Boolean
    ): ViewItem {

        return SOSCardViewItem(
            id = ID_TIMEOUT,
            title = buildTimeoutTitle(resources, timeout).asBodyText(resources),
            icon = BigImage(R.drawable.ic_clock),
            isEnabled = isEnabledDraft
        )
    }

    private fun buildExclusionHeader(resources: Map<String, Any>, isEnabledDraft: Boolean): ViewItem {

        return SOSSectionHeaderViewItem(
            title = resources.getString(R.string.sos_exclusion_periods_header).asTitleText(resources),
            icon = BigImage(R.drawable.ic_bed),
            isEnabled = isEnabledDraft
        )
    }

    private fun buildPeriodCards(
        resources: Map<String, Any>,
        periods: List<ExclusionPeriod>,
        isEnabledDraft: Boolean
    ): List<ViewItem> {

        return periods.mapIndexed { index, period ->

            SOSCardViewItem(
                id = ID_PERIOD_ITEM_BASE + index,
                title = period.formatPeriod(resources).asBodyText(resources),
                icon = BigImage(R.drawable.ic_bed),
                endIcon = BigImage(R.drawable.ic_clear),
                isEnabled = isEnabledDraft
            )
        }
    }

    private fun buildAddPeriodCard(resources: Map<String, Any>, isEnabledDraft: Boolean): ViewItem {

        return SOSCardViewItem(
            id = ID_ADD_PERIOD,
            title = resources.getString(R.string.sos_add_exclusion_period).asBodyText(resources),
            desc = resources.getString(R.string.sos_add_exclusion_period_desc).asSecondaryBodyText(resources),
            icon = BigImage(R.drawable.ic_add),
            isEnabled = isEnabledDraft
        )
    }

    private fun ExclusionPeriod.formatPeriod(resources: Map<String, Any>): String {

        return resources.getString(R.string.sos_exclusion_period_format)
            .format(startHour, startMinute, endHour, endMinute)
    }

    private fun String.asTitleText(resources: Map<String, Any>): BigText {

        return withStyleTitleLarge()
            .with(BigForegroundColor(resources.textColorPrimary))
            .build()
    }

    private fun String.asBodyText(resources: Map<String, Any>): BigText {

        return withStyleBodyLarge()
            .with(BigForegroundColor(resources.textColorPrimary))
            .build()
    }

    private fun String.asPrimaryBodyMediumText(resources: Map<String, Any>): BigText {

        return withStyleBodyMedium()
            .with(BigForegroundColor(resources.textColorPrimary))
            .build()
    }

    private fun String.asSecondaryBodyText(resources: Map<String, Any>): BigText {

        return withStyleBodyMedium()
            .with(BigForegroundColor(resources.textColorSecondary))
            .build()
    }

    fun toggleFeatureDraft() {

        isFeatureEnabledDraft.value = !isFeatureEnabledDraft.value
    }

    fun updateTimeout(timeoutMillis: Long) {

        timeout.value = timeoutMillis
    }

    private fun buildTimeoutTitle(resources: Map<String, Any>, timeoutMillis: Long): String {

        if (BuildConfig.DEBUG && timeoutMillis < HOUR_MILLIS) {

            val seconds = (timeoutMillis / SECOND_MILLIS).toInt()
            return resources.getString(R.string.sos_timeout_seconds).format(seconds)
        }

        val hours = (timeoutMillis / HOUR_MILLIS).toInt()
        return resources.getString(R.string.sos_timeout_value).format(hours)
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

        private const val SECOND_MILLIS = 1000L
        private const val HOUR_MILLIS = 60 * 60 * 1000L
    }
}
