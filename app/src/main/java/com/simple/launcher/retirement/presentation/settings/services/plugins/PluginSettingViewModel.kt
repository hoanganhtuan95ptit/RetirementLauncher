package com.simple.launcher.retirement.presentation.settings.services.plugins

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ViewItemViewModel
import com.simple.launcher.retirement.presentation.settings.services.settingHeader
import kotlinx.coroutines.flow.first

class PluginSettingViewModel : ViewItemViewModel() {

    override suspend fun wrapViewItem(values: MutableList<ViewItem>): List<ViewItem> {

        if (values.isNotEmpty()) settingHeader(
            title = R.string.setting_header_plugins,
            resources = resources.first()
        ).let {

            values.add(0, it)
        }

        return super.wrapViewItem(values)
    }
}
