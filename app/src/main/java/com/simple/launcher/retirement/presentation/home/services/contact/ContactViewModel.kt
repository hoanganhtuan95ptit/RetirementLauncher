package com.simple.launcher.retirement.presentation.home.services.contact

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.usecase.GetHomeContactUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.sp
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.size.DP
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.toBuilder
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.StateFlow

class ContactViewModel : BaseViewModel() {

    val contactViewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        flow2 = GetHomeContactUseCase.instance.invoke(),
        initialValue = null
    ) { resources, contacts ->

        buildContactGroup(resources = resources, contacts = contacts)
    }

    private fun buildContactGroup(
        resources: Map<String, Any>,
        contacts: List<HomeContentEntity.Contact>
    ): GroupViewItem {

        val textColor = resources.textColorPrimary
        val tapToCallLabel = resources.getString(R.string.contact_tap_to_call)
        val items = buildList<ViewItem> {

            buildHeader(resources)?.let(::add)
            addAll(
                contacts.map {
                    it.toViewItem(textColor = textColor, tapToCallLabel = tapToCallLabel)
                }
            )
        }

        return GroupViewItem(order = 2, list = items)
    }

    private fun buildHeader(resources: Map<String, Any>): HeaderHomeItem? {

        val title = resources.getString(R.string.home_header_contacts)
        if (title.isBlank()) {

            return null
        }

        return HeaderHomeItem(
            title = title
                .toBuilder()
                .with(BigTextSize(22.sp().toInt()), BigForegroundColor(Color.WHITE))
                .build()
        )
    }

    private fun HomeContentEntity.Contact.toViewItem(
        textColor: Int,
        tapToCallLabel: String
    ): ContactHomeItem = ContactHomeItem(
        entity = entity,
        name = entity.name
            .toBuilder()
            .with(BigForegroundColor(textColor))
            .build(),
        photo = if (entity.photoUri != null) {
            BigImage(entity.photoUri)
        } else {
            BigImage(R.drawable.ic_home_contact_24dp)
        },
        background = Background.Builder()
            .backgroundColor(Color.WHITE)
            .cornerRadius(DP.DP_24)
            .build(),

        tapToCallLabel = tapToCallLabel
            .toBuilder()
            .with(BigForegroundColor(textColor))
            .build(),
        tapToCallBackground = Background.Builder()
            .backgroundColor("#F0F0F0".toColorInt())
            .cornerRadius(DP.DP_24)
            .build(),
    )
}
