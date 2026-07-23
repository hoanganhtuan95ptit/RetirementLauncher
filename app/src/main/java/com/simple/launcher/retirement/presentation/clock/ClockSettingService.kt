package com.simple.launcher.retirement.presentation.clock

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.launchCollect
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.plugins.PluginSettingService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [SettingsFragment::class])
class ClockSettingService : PluginSettingService() {

    private lateinit var viewModel: ClockSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {
        super.setup(settingsFragment)

        viewModel = settingsFragment.viewModels<ClockSettingViewModel>().value

        viewModel.viewItemList.launchCollect(settingsFragment) {

            pluginSettingViewModel.updateItem(it)
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(settingsFragment.viewLifecycleOwner) { event ->

            val item = event.item
            if (item.id == SettingItem.ID_LUNAR_CALENDAR_TOGGLE) {

                sendDeeplink(DeepLinks.CLOCK_SETTING)
            }
        }
    }
}
