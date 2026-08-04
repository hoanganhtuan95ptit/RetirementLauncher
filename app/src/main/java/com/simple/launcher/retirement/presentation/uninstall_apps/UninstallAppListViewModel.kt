package com.simple.launcher.retirement.presentation.uninstall_apps

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.SearchState
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildSearchState
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.VietnameseStringUtils
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSurface
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.toBig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * ViewModel cho màn xoá app.
 * Danh sách chỉ bao gồm những app KHÔNG phải default/system (thông qua
 * AppRepository.isDefaultApp) và loại trừ chính app của launcher.
 */
class UninstallAppListViewModel : BaseViewModel() {

    private val appRepository: AppRepository = AppRepository.instance

    val query = MutableStateFlow("")

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->

        val color = resources.textColorPrimary
        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.uninstall_apps_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val searchState: StateFlow<SearchState> = combineState(
        flow1 = resources,
        initialValue = SearchState.empty()
    ) { resources ->

        val textColor = resources.textColorPrimary
        val hintColor = resources.textColorSecondary
        val backgroundColor = resources.colorSurface

        value = buildSearchState(
            hint = resources.getString(R.string.search),
            textColor = textColor,
            hintColor = hintColor,
            backgroundColor = backgroundColor
        )
    }

    private val _selectedIds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    /**
     * Nguồn app hiển thị = tất cả launcher apps trừ default/system + trừ chính app này.
     * Filter chạy trên IO thread thông qua Flow của AppRepository.
     */
    private val uninstallableApps: StateFlow<List<PreparedAppEntry>?> = mutableStateFlow(null) {

        val ownPackage = MainApplication.instance.packageName
        appRepository.getAllAppFlow().collect { apps ->

            value = apps
                .asSequence()
                .filter { it.packageName != ownPackage }
                .filterNot { appRepository.isDefaultApp(it.packageName) }
                .map { entity ->

                    val rawLabel = entity.label
                    PreparedAppEntry(
                        entity = entity,
                        lowerLabel = rawLabel.lowercase(),
                        normalizedLabel = VietnameseStringUtils.normalizeForSearch(rawLabel),
                        bigLabel = rawLabel.toBig(),
                        bigIcon = BigImage(entity.icon)
                    )
                }
                .toList()
        }
    }

    val items: StateFlow<List<UninstallAppItem>> = combineState(
        flow1 = uninstallableApps.filterNotNull(),
        flow2 = query,
        flow3 = _selectedIds,
        initialValue = emptyList()
    ) { prepared, query, selectedIds ->

        val filtered = filterByQuery(prepared, query)
        value = filtered
            .sortedWith(compareBy({ it.second }, { it.first.lowerLabel }))
            .map { (entry, _) -> toUninstallAppItem(entry, selectedIds) }
    }

    val deleteAction: StateFlow<ActionState> = combineState(
        flow1 = resources,
        flow2 = _selectedIds,
        initialValue = ActionState.empty()
    ) { resources, selectedIds ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary
        val text = String.format(resources.getString(R.string.uninstall_apps_action), selectedIds.size)

        value = buildActionState(
            text = text,
            textColor = color,
            backgroundColor = backgroundColor,
            isEnabled = selectedIds.isNotEmpty()
        )
    }

    fun search(text: String) {

        query.value = text
    }

    fun toggle(entity: AppEntity) {

        val packageName = entity.packageName
        val current = _selectedIds.value
        _selectedIds.value = if (packageName in current) {

            current - packageName
        } else {

            current + packageName
        }
    }

    /** Trả về danh sách package đã chọn theo thứ tự alphabet để việc show popup tuần tự
     *  có thứ tự ổn định giữa các lần thao tác. */
    fun getSelectedPackagesOrdered(): List<String> {

        val selected = _selectedIds.value
        return uninstallableApps.value
            .orEmpty()
            .map { it.entity.packageName }
            .filter { it in selected }
    }

    /** Sau khi user xoá xong 1 package (thành công), loại nó khỏi selection để list refresh. */
    fun onPackageUninstalled(packageName: String) {

        _selectedIds.value = _selectedIds.value - packageName
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun filterByQuery(
        apps: List<PreparedAppEntry>,
        query: String
    ): List<Pair<PreparedAppEntry, Int>> {

        if (query.isBlank()) return apps.map { it to 0 }

        val normalizedQuery = VietnameseStringUtils.normalizeForSearch(query)
        return apps.mapNotNull { entry -> matchWithPriority(entry, normalizedQuery) }
    }

    private fun matchWithPriority(
        entry: PreparedAppEntry,
        normalizedQuery: String
    ): Pair<PreparedAppEntry, Int>? {

        val normalizedLabel = entry.normalizedLabel
        val priority = when {

            normalizedLabel == normalizedQuery -> 0
            normalizedLabel.startsWith(normalizedQuery) -> 1
            normalizedLabel.contains(normalizedQuery) -> 2
            else -> return null
        }
        return entry to priority
    }

    private fun toUninstallAppItem(
        entry: PreparedAppEntry,
        selectedIds: Set<String>
    ): UninstallAppItem = UninstallAppItem(
        label = entry.bigLabel,
        icon = entry.bigIcon,
        isSelected = entry.entity.packageName in selectedIds,
        entity = entry.entity
    )

    private data class PreparedAppEntry(
        val entity: AppEntity,
        val lowerLabel: String,
        val normalizedLabel: String,
        val bigLabel: BigText,
        val bigIcon: BigImage
    )
}
