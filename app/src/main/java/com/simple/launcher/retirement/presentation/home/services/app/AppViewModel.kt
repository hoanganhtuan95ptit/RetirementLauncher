package com.simple.launcher.retirement.presentation.home.services.app

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull

class AppViewModel : BaseViewModel() {

    val apps: StateFlow<List<HomeContentEntity.App>?> = mutableStateFlow(
        null
    ) {

        GetHomeAppsUseCase.instance.invoke().collect {

            value = it
        }
    }

    val appViewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        flow2 = apps.filterNotNull(),
        initialValue = null
    ) { resources, apps ->

        value = buildAppGroup(resources = resources, apps = apps)
    }

    private fun buildAppGroup(
        resources: Map<String, Any>,
        apps: List<HomeContentEntity.App>
    ): GroupViewItem {

        val screenWidth = calculateScreenWidth()
        val items = buildList<ViewItem> {

            if (apps.isNotEmpty()) buildHeader(resources, screenWidth)?.let(::add)
            addAll(apps.map { it.toViewItem(resources, screenWidth) })
        }

        return GroupViewItem(order = 1, list = items)
    }

    private fun calculateScreenWidth(): Int {

        return android.content.res.Resources.getSystem().displayMetrics.widthPixels - 2 * 12.dp().toInt()
    }

    private fun HomeContentEntity.App.toViewItem(
        resources: Map<String, Any>,
        screenWidth: Int
    ): AppHomeItem {

        return AppHomeItem(entity = entity, screenWidth = screenWidth).apply {

            buildDrawSpec(resources)
        }
    }

    private fun buildHeader(resources: Map<String, Any>, screenWidth: Int): HeaderHomeItem? {

        val title = resources.getString(R.string.home_header_apps)
        if (title.isBlank()) {

            return null
        }

        return HeaderHomeItem(
            title = title,
            screenWidth = screenWidth
        ).apply {

            buildDrawSpec(resources)
        }
    }
}
