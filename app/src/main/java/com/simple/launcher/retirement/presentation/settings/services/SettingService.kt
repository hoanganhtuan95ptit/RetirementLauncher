package com.simple.launcher.retirement.presentation.settings.services

import androidx.fragment.app.Fragment
import com.simple.component.service.FragmentViewCreatedService
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.presentation.settings.adapters.SettingHeaderItem
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.exts.colorBackground
import com.simple.launcher.retirement.utils.exts.colorOnPrimaryContainer
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimaryContainer
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.withAlpha
import com.simple.launcher.retirement.utils.exts.withStyleBodyLarge
import com.simple.launcher.retirement.utils.exts.withStyleBodyMedium
import com.simple.launcher.retirement.utils.exts.withStyleTitleLarge
import com.simple.ui.precompute.image.ColorFilter
import com.simple.ui.precompute.image.addTransform
import com.simple.ui.precompute.image.build
import com.simple.ui.precompute.image.toBuilder
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with

fun settingItem(
    id: Int,
    icon: Int,
    title: Int,
    description: Int? = null,
    isSwitch: Boolean = false,
    isChecked: Boolean = false,
    highlight: Boolean = false,
    resources: Map<String, Any>
): SettingItem = SettingItem(
    id = id,

    title = resources.getString(title)
        .withStyleBodyLarge()
        .with(BigForegroundColor(resources.textColorPrimary))
        .build(),

    description = if (description != null) {

        resources.getString(description)
            .withStyleBodyMedium()
            .with(BigForegroundColor(resources.textColorPrimary.withAlpha(0.7f)))
            .build()
    } else {

        null
    },

    icon = icon.toBuilder()
        .addTransform(ColorFilter(resources.colorOnPrimaryContainer))
        .build(),
    iconBackground = Background.Builder()
        .backgroundColor(if (highlight) resources.colorOnPrimaryContainer.withAlpha(0.2f) else resources.colorPrimaryContainer)
        .cornerRadius(24.dp())
        .build(),

    isSwitch = isSwitch,
    isChecked = isChecked,

    background = if (highlight) {

        Background.Builder()
            .backgroundColor(resources.colorPrimaryContainer)
            .cornerRadius(24.dp())
            .stroke(2.dp(), resources.colorPrimary, dashGap = 4.dp().toInt(), dashWidth = 4.dp().toInt())
            .build()
    } else {

        Background.Builder()
            .backgroundColor(resources.colorBackground)
            .cornerRadius(24.dp())
            .stroke(2.dp(), resources.colorPrimary, dashGap = 4.dp().toInt(), dashWidth = 4.dp().toInt())
            .build()
    },
)

// Header và item dùng chung một style để các group settings render đồng nhất.
fun settingHeader(
    title: Int,
    resources: Map<String, Any>
): SettingHeaderItem = SettingHeaderItem(
    title = resources.getString(title)
        .withStyleTitleLarge()
        .with(BigBold, BigForegroundColor(resources.textColorPrimary))
        .build()
)

abstract class SettingService : FragmentViewCreatedService {

    protected lateinit var settingsViewModel: SettingsViewModel

    abstract fun setup(settingsFragment: SettingsFragment)

    final override fun setup(fragment: Fragment) {

        if (fragment is SettingsFragment) {

            settingsViewModel = fragment.viewModel

            setup(settingsFragment = fragment)
        }
    }
}
