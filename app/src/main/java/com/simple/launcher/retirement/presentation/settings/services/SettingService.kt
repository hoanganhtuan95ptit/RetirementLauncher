package com.simple.launcher.retirement.presentation.settings.services

import androidx.fragment.app.Fragment
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
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.services.FragmentViewCreatedService
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.build
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import com.simple.launcher.retirement.utils.text.withStyleTitleLarge

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
        .with(ForegroundColor(resources.textColorPrimary))
        .build(),
    icon = ImageRes(icon),
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
        .with(ForegroundColor(resources.textColorPrimary), Bold)
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
