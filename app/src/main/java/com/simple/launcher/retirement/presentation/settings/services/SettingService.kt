package com.simple.launcher.retirement.presentation.settings.services

import androidx.fragment.app.Fragment
import com.simple.component.service.FragmentViewCreatedService
import com.simple.launcher.retirement.presentation.settings.SettingHeaderItem
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.exts.colorBackground
import com.simple.launcher.retirement.utils.exts.colorOnSurface
import com.simple.launcher.retirement.utils.exts.colorOnSurfaceVariant
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import com.simple.launcher.retirement.utils.text.withStyleTitleLarge
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with

fun settingItem(
    id: Int,
    icon: Int,
    title: Int,
    isSwitch: Boolean = false,
    isChecked: Boolean = false,
    resources: Map<String, Any>
): SettingItem = SettingItem(
    id = id,
    title = resources.getString(title)
        .withStyleBodyLarge()
        .with(BigForegroundColor(resources.textColorPrimary))
        .build(),
    icon = BigImage(icon),
    iconBackground = Background.Builder()
        .backgroundColor(resources.colorOnSurface)
        .cornerRadius(DP.DP_24)
        .build(),
    isSwitch = isSwitch,
    isChecked = isChecked,
    background = Background.Builder()
        .backgroundColor(resources.colorBackground)
        .cornerRadius(DP.DP_24)
        .stroke(DP.DP_1, resources.colorOnSurfaceVariant)
        .build(),
)

// Header và item dùng chung một style để các group settings render đồng nhất.
fun settingHeader(
    title: Int,
    resources: Map<String, Any>
): SettingHeaderItem = SettingHeaderItem(
    title = resources.getString(title)
        .withStyleTitleLarge()
        .with(BigForegroundColor(resources.textColorPrimary), BigBold)
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
