package com.simple.launcher.retirement.presentation.notification_block

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.presentation.emergency.adapters.SOSCardViewItem
import com.simple.launcher.retirement.presentation.emergency.adapters.SOSSectionHeaderViewItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorOutline
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSecondaryContainer
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.exts.withStyleBodyLarge
import com.simple.launcher.retirement.utils.exts.withStyleBodyMedium
import com.simple.launcher.retirement.utils.exts.withStyleTitleLarge
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.toBig
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull

class NotificationBlockSettingsViewModel : BaseViewModel() {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private val appRepository: AppRepository = AppRepository.instance
    private val preferenceRepository = PreferenceRepository.instance

    // ── 2. Flows ──────────────────────────────────────────────────────────

    val isFeatureEnabledDraft: MutableStateFlow<Boolean> =
        MutableStateFlow(preferenceRepository.isNotificationBlockEnabled())

    val blockedPackagesDraft: MutableStateFlow<Set<String>> =
        MutableStateFlow(preferenceRepository.getNotificationBlockedPackages())

    val retentionMillisDraft: MutableStateFlow<Long> =
        MutableStateFlow(preferenceRepository.getNotificationRetentionMillis())

    val saveResultFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->

        val color = resources.textColorPrimary
        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.notification_block_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val saveAction: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        value = buildActionState(
            text = resources.getString(R.string.notification_block_save),
            textColor = resources.colorOnPrimary,
            backgroundColor = resources.colorPrimary,
            strokeWidth = 1.dp().toInt(),
            strokeColor = resources.colorOutline
        )
    }

    private val installedApps: StateFlow<List<PreparedAppEntry>?> = mutableStateFlow(null) {

        val ownPackage = MainApplication.instance.packageName
        appRepository.getAllAppFlow().collect { apps ->

            value = apps
                .asSequence()
                .filter { it.packageName != ownPackage }
                .map { entity ->

                    PreparedAppEntry(
                        entity = entity,
                        lowerLabel = entity.label.lowercase(),
                        bigLabel = entity.label.toBig(),
                        bigIcon = BigImage(entity.icon)
                    )
                }
                .sortedBy { it.lowerLabel }
                .toList()
        }
    }

    val viewItemList: StateFlow<List<ViewItem>> = combineState(
        flow1 = resources,
        flow2 = installedApps.filterNotNull(),
        flow3 = blockedPackagesDraft,
        flow4 = retentionMillisDraft,
        flow5 = isFeatureEnabledDraft,
        initialValue = emptyList()
    ) { resources, apps, blocked, retention, isEnabled ->

        value = buildViewItems(resources, apps, blocked, retention, isEnabled)
    }

    // ── 3. Public API ─────────────────────────────────────────────────────

    fun toggleFeatureDraft() {

        isFeatureEnabledDraft.value = !isFeatureEnabledDraft.value
    }

    fun toggleApp(entity: AppEntity) {

        val packageName = entity.packageName
        val current = blockedPackagesDraft.value
        blockedPackagesDraft.value = if (packageName in current) {

            current - packageName
        } else {

            current + packageName
        }
    }

    fun updateRetentionDraft(millis: Long) {

        retentionMillisDraft.value = millis
    }

    fun save() {

        preferenceRepository.setNotificationBlockEnabled(isFeatureEnabledDraft.value)
        preferenceRepository.setNotificationBlockedPackages(blockedPackagesDraft.value)
        preferenceRepository.setNotificationRetentionMillis(retentionMillisDraft.value)
        saveResultFlow.tryEmit(true)
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun buildViewItems(
        resources: Map<String, Any>,
        apps: List<PreparedAppEntry>,
        blocked: Set<String>,
        retention: Long,
        isEnabled: Boolean
    ): List<ViewItem> {

        val items = mutableListOf<ViewItem>()

        items += buildMasterToggleHeader(resources, isEnabled)
        items += buildRetentionSectionHeader(resources, isEnabled)
        items += buildRetentionCard(resources, retention, isEnabled)
        items += buildAppsSectionHeader(resources, isEnabled)
        items += buildAppItems(apps, blocked)

        return items
    }

    private fun buildMasterToggleHeader(resources: Map<String, Any>, isEnabled: Boolean): ViewItem {

        return NotificationHeaderViewItem(
            title = resources.getString(R.string.notification_block_title).asTitleText(resources),
            desc = resources.getString(R.string.setting_notification_block_desc)
                .asSecondaryBodyText(resources),
            icon = BigImage(R.drawable.ic_notification_block_24dp),
            iconBackground = Background.Builder()
                .backgroundColor(resources.colorSecondaryContainer)
                .cornerRadius(40.dp().toInt())
                .build(),
            isEnabled = isEnabled
        )
    }

    private fun buildRetentionSectionHeader(resources: Map<String, Any>, isEnabled: Boolean): ViewItem {

        return SOSSectionHeaderViewItem(
            title = resources.getString(R.string.notification_block_retention_title).asTitleText(resources),
            icon = BigImage(R.drawable.ic_clock),
            isEnabled = isEnabled
        )
    }

    private fun buildRetentionCard(resources: Map<String, Any>, retention: Long, isEnabled: Boolean): ViewItem {

        return SOSCardViewItem(
            id = ID_RETENTION_CARD,
            title = buildRetentionLabel(resources, retention).asBodyText(resources),
            desc = resources.getString(R.string.notification_block_retention_card_desc)
                .asSecondaryBodyText(resources),
            icon = BigImage(R.drawable.ic_clock),
            isEnabled = isEnabled
        )
    }

    private fun buildAppsSectionHeader(resources: Map<String, Any>, isEnabled: Boolean): ViewItem {

        return SOSSectionHeaderViewItem(
            title = resources.getString(R.string.notification_block_apps_header).asTitleText(resources),
            icon = BigImage(R.drawable.ic_notification_block_24dp),
            isEnabled = isEnabled
        )
    }

    private fun buildAppItems(
        apps: List<PreparedAppEntry>,
        blocked: Set<String>
    ): List<ViewItem> {

        return apps.map { entry ->

            NotificationBlockAppItem(
                label = entry.bigLabel,
                icon = entry.bigIcon,
                isSelected = entry.entity.packageName in blocked,
                entity = entry.entity
            )
        }
    }

    private fun buildRetentionLabel(resources: Map<String, Any>, millis: Long): String {

        if (millis <= 0L) {

            return resources.getString(R.string.notification_block_retention_off)
        }

        // DEBUG cho phép chọn mốc dưới 1 giờ (2/5/15/30 phút) — hiển thị theo phút để test.
        if (BuildConfig.DEBUG && millis < HOUR_MILLIS) {

            val minutes = (millis / MINUTE_MILLIS).toInt()
            return resources.getString(R.string.notification_block_retention_minutes).format(minutes)
        }

        val hours = (millis / HOUR_MILLIS).toInt()
        return resources.getString(R.string.notification_block_retention_hours).format(hours)
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

    private fun String.asSecondaryBodyText(resources: Map<String, Any>): BigText {

        return withStyleBodyMedium()
            .with(BigForegroundColor(resources.textColorSecondary))
            .build()
    }

    // ── 5. Nested classes ─────────────────────────────────────────────────

    private data class PreparedAppEntry(
        val entity: AppEntity,
        val lowerLabel: String,
        val bigLabel: BigText,
        val bigIcon: BigImage
    )

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        const val ID_RETENTION_CARD = 300

        private const val MINUTE_MILLIS = 60 * 1000L
        private const val HOUR_MILLIS = 60 * 60 * 1000L
    }
}
