package com.simple.launcher.retirement.presentation.home.services.contact

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.usecase.GetHomeContactUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull

class ContactViewModel : BaseViewModel() {

    val contacts: StateFlow<List<HomeContentEntity.Contact>?> = mutableStateFlow(
        null
    ) {

        GetHomeContactUseCase.instance.invoke().collect {
            value = it
        }
    }

    val contactViewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        flow2 = contacts.filterNotNull(),
        initialValue = null
    ) { resources, contacts ->

        value = buildContactGroup(resources = resources, contacts = contacts)
    }

    private fun buildContactGroup(
        resources: Map<String, Any>,
        contacts: List<HomeContentEntity.Contact>
    ): GroupViewItem {

        val screenWidth = calculateScreenWidth()
        val items = buildList<ViewItem> {

            if (contacts.isNotEmpty()) buildHeader(resources, screenWidth)?.let(::add)
            addAll(contacts.map { it.toViewItem(resources, screenWidth) })
        }

        return GroupViewItem(order = 2, list = items)
    }

    private fun calculateScreenWidth(): Int {

        return android.content.res.Resources.getSystem().displayMetrics.widthPixels - 2 * 12.dp().toInt()
    }

    private fun HomeContentEntity.Contact.toViewItem(
        resources: Map<String, Any>,
        screenWidth: Int
    ): ContactHomeItem {

        return ContactHomeItem(entity = entity, screenWidth = screenWidth).apply {

            buildDrawSpec(resources)
        }
    }

    private fun buildHeader(resources: Map<String, Any>, screenWidth: Int): HeaderHomeItem? {

        val title = resources.getString(R.string.home_header_contacts)
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
