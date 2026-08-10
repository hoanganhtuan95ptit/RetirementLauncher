package com.simple.launcher.retirement.presentation.emergency.setting

import androidx.lifecycle.viewModelScope
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import com.simple.launcher.retirement.domain.model.SOSConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.domain.usecase.SetEmergencyCallEnabledUseCase
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.presentation.emergency.adapters.SOSCardViewItem
import com.simple.launcher.retirement.presentation.emergency.adapters.SOSHeaderViewItem
import com.simple.launcher.retirement.presentation.emergency.adapters.SOSSectionHeaderViewItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorOutline
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSecondaryContainer
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.exts.withStyleBodyLarge
import com.simple.launcher.retirement.utils.exts.withStyleBodyMedium
import com.simple.launcher.retirement.utils.exts.withStyleTitleLarge
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SOSSettingsViewModel : BaseViewModel() {

    private val preferenceRepository = PreferenceRepository.instance
    private val setEmergencyCallEnabledUseCase = SetEmergencyCallEnabledUseCase.instance

    val saveResultFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    val timeout = MutableStateFlow(preferenceRepository.getEmergencyTimeout())

    val exclusionPeriods = MutableStateFlow(preferenceRepository.getExclusionPeriods())

    val isFeatureEnabledDraft = MutableStateFlow(preferenceRepository.isEmergencyCallEnabled())

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
        )
    }

    val viewItemList: StateFlow<List<ViewItem>> = combineState(
        resources,
        isFeatureEnabledDraft,
        timeout,
        exclusionPeriods,
        emptyList()
    ) { resources, isEnabledDraft, timeout, periods ->

        value = buildSosSettingItems(resources, isEnabledDraft, timeout, periods)
    }

    private fun buildSosSettingItems(
        resources: Map<String, Any>,
        isEnabledDraft: Boolean,
        timeout: Long,
        periods: List<ExclusionPeriod>
    ): List<ViewItem> {

        // Các item luôn hiển thị đủ để người dùng xem cấu hình,
        // nhưng sẽ mờ/khóa khi master toggle đang tắt.
        return listOf(
            buildMasterToggleItem(resources, isEnabledDraft),
            buildTimeoutSectionHeaderItem(resources, isEnabledDraft),
            buildTimeoutSelectorItem(resources, timeout, isEnabledDraft),
            buildExclusionSectionHeaderItem(resources, isEnabledDraft)
        ) + buildExclusionPeriodItems(resources, periods, isEnabledDraft) +
            buildAddExclusionPeriodItem(resources, isEnabledDraft)
    }

    private fun buildMasterToggleItem(resources: Map<String, Any>, isEnabledDraft: Boolean): ViewItem {

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

    private fun buildTimeoutSectionHeaderItem(resources: Map<String, Any>, isEnabledDraft: Boolean): ViewItem {

        return SOSSectionHeaderViewItem(
            title = resources.getString(R.string.sos_timeout_label).asTitleText(resources),
            icon = BigImage(R.drawable.ic_clock),
            isEnabled = isEnabledDraft
        )
    }

    private fun buildTimeoutSelectorItem(
        resources: Map<String, Any>,
        timeout: Long,
        isEnabledDraft: Boolean
    ): ViewItem {

        return SOSCardViewItem(
            id = ID_TIMEOUT,
            title = buildTimeoutLabel(resources, timeout).asBodyText(resources),
            desc = resources.getString(R.string.sos_timeout_desc).asSecondaryBodyText(resources),
            icon = BigImage(R.drawable.ic_clock),
            isEnabled = isEnabledDraft
        )
    }

    private fun buildExclusionSectionHeaderItem(resources: Map<String, Any>, isEnabledDraft: Boolean): ViewItem {

        return SOSSectionHeaderViewItem(
            title = resources.getString(R.string.sos_exclusion_periods_header).asTitleText(resources),
            icon = BigImage(R.drawable.ic_bed),
            isEnabled = isEnabledDraft
        )
    }

    private fun buildExclusionPeriodItems(
        resources: Map<String, Any>,
        periods: List<ExclusionPeriod>,
        isEnabledDraft: Boolean
    ): List<ViewItem> {

        return periods.mapIndexed { index, period ->

            SOSCardViewItem(
                id = ID_PERIOD_ITEM_BASE + index,
                title = period.formatAsTimeRange(resources).asBodyText(resources),
                icon = BigImage(R.drawable.ic_bed),
                endIcon = BigImage(R.drawable.ic_clear),
                isEnabled = isEnabledDraft
            )
        }
    }

    private fun buildAddExclusionPeriodItem(resources: Map<String, Any>, isEnabledDraft: Boolean): ViewItem {

        return SOSCardViewItem(
            id = ID_ADD_PERIOD,
            title = resources.getString(R.string.sos_add_exclusion_period).asBodyText(resources),
            desc = resources.getString(R.string.sos_add_exclusion_period_desc).asSecondaryBodyText(resources),
            icon = BigImage(R.drawable.ic_add),
            isEnabled = isEnabledDraft
        )
    }

    private fun ExclusionPeriod.formatAsTimeRange(resources: Map<String, Any>): String {

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

    fun toggleEmergencyFeatureDraft() {

        isFeatureEnabledDraft.value = !isFeatureEnabledDraft.value
    }

    fun updateTimeoutDraft(timeoutMillis: Long) {

        timeout.value = timeoutMillis
    }

    private fun buildTimeoutLabel(resources: Map<String, Any>, timeoutMillis: Long): String {

        // < 1 phut (debug seconds): hien theo giay.
        if (BuildConfig.DEBUG && timeoutMillis < MINUTE_MILLIS) {

            val seconds = (timeoutMillis / SECOND_MILLIS).toInt()
            return resources.getString(R.string.sos_timeout_seconds).format(seconds)
        }

        // < 1 gio: hien theo phut.
        if (timeoutMillis < HOUR_MILLIS) {

            val minutes = (timeoutMillis / MINUTE_MILLIS).toInt()
            return resources.getString(R.string.sos_timeout_minutes).format(minutes)
        }

        val hours = (timeoutMillis / HOUR_MILLIS).toInt()
        return resources.getString(R.string.sos_timeout_value).format(hours)
    }

    fun addExclusionPeriodDraft(period: ExclusionPeriod) {

        exclusionPeriods.value += period
    }

    fun removeExclusionPeriodDraft(id: String) {

        exclusionPeriods.value = exclusionPeriods.value.filter { it.id != id }
    }

    fun saveEmergencyConfig() {

        val config = buildDraftConfig()

        // Lưu trước khi xin quyền để nếu Activity/ViewModel bị hủy khi đổi default launcher,
        // Activity mới vẫn có thể apply tiếp cấu hình đang dở.
        preferenceRepository.setPendingEmergencyConfig(config)

        viewModelScope.launch {

            // SharedFlow không replay để màn mở lại không nhận lại kết quả cũ.
            saveResultFlow.emit(setEmergencyCallEnabledUseCase(config))
        }
    }

    private fun buildDraftConfig(): SOSConfig {

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
        private const val MINUTE_MILLIS = 60 * 1000L
        private const val HOUR_MILLIS = 60 * 60 * 1000L
    }
}
