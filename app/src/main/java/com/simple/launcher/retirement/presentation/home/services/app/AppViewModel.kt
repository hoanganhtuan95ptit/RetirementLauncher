package com.simple.launcher.retirement.presentation.home.services.app

import android.graphics.Color
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase2
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.build
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.text.withStyleTitleLarge
import kotlinx.coroutines.flow.StateFlow

class AppViewModel : BaseViewModel() {

    val appViewItemList: StateFlow<GroupViewItem?> = combineState(
        resources,
        GetHomeAppsUseCase2.instance.invoke(),
        null
    ) { resources, apps ->

        val list = arrayListOf<ViewItem>()

        if (apps.isNotEmpty()) HeaderHomeItem(
            title = resources.getString(R.string.home_header_apps)
                .withStyleTitleLarge()
                .with(ForegroundColor(Color.WHITE))
                .build()
        ).let {
            list.add(it)
        }

        apps.map {
            it.toViewItem()
        }.let {
            list.addAll(it)
        }

        GroupViewItem(1, list)
    }

    private fun HomeContentEntity.App.toViewItem() = AppHomeItem(
        entity = entity,
        icon = ImageDrawable(entity.icon),
        label = RichText(entity.label),
        background = Background.Builder()
            .backgroundColor(Color.WHITE)
            .cornerRadius(DP.DP_24)
            .build()
    )
}