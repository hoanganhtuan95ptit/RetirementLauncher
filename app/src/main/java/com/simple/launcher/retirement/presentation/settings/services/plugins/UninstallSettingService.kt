package com.simple.launcher.retirement.presentation.settings.services.plugins

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.observe
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [SettingsFragment::class])
class UninstallSettingService : PluginSettingService() {

    private lateinit var viewModel: UninstallSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {

        super.setup(settingsFragment)

        viewModel = settingsFragment.viewModels<UninstallSettingViewModel>().value

        viewModel.viewItemList.observe(settingsFragment) {

            pluginSettingViewModel.updateItem(it)
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().observe(settingsFragment.viewLifecycleOwner) { event ->

            if (event.item.id == SettingItem.ID_UNINSTALL_APPS) {

                sendDeeplinkWithBackStack(DeepLinks.UNINSTALL_APPS)
            }
        }
    }
}
