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
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.build
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.text.withStyleTitleLarge
import kotlinx.coroutines.flow.StateFlow

class ContactViewModel : BaseViewModel() {

    val contactViewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        GetHomeContactUseCase.instance.invoke(),
        null
    ) { resources, contacts ->

        val list = arrayListOf<ViewItem>()


        if (contacts.isNotEmpty()) HeaderHomeItem(
            title = resources.getString(R.string.home_header_contacts)
                .withStyleTitleLarge()
                .with(ForegroundColor(Color.WHITE))
                .build()
        ).let {

            list.add(it)
        }

        val textColor = resources.textColorPrimary
        val tapToCallLabel = resources.getString(R.string.contact_tap_to_call)

        contacts.map {

            it.toViewItem(textColor = textColor, tapToCallLabel = tapToCallLabel)
        }.let {
            list.addAll(it)
        }


        GroupViewItem(2, list)
    }

    private fun HomeContentEntity.Contact.toViewItem(textColor: Int, tapToCallLabel: String) = ContactHomeItem(
        entity = entity,
        name = entity.name
            .with(ForegroundColor(textColor))
            .build(),
        photo = if (entity.photoUri != null) {
            ImagePath(entity.photoUri)
        } else {
            ImageRes(R.drawable.ic_home_contact_24dp)
        },
        background = Background.Builder()
            .backgroundColor(Color.WHITE)
            .cornerRadius(DP.DP_24)
            .build(),

        tapToCallLabel = tapToCallLabel
            .with(ForegroundColor(textColor))
            .build(),
        tapToCallBackground = Background.Builder()
            .backgroundColor("#F0F0F0".toColorInt())
            .cornerRadius(DP.DP_24)
            .build(),
    )
}