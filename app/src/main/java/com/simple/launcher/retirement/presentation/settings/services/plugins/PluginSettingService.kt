package com.simple.launcher.retirement.presentation.settings.services.plugins

import androidx.annotation.CallSuper
import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.SettingService
import com.simple.launcher.retirement.utils.lifecycle.observe

@AutoRegister(apis = [SettingsFragment::class])
open class PluginSettingService : SettingService() {

    protected lateinit var pluginSettingViewModel: PluginSettingViewModel

    @CallSuper
    override fun setup(settingsFragment: SettingsFragment) {

        pluginSettingViewModel = settingsFragment.viewModels<PluginSettingViewModel>().value

        pluginSettingViewModel.viewItemList.observe(settingsFragment.viewLifecycleOwner) {

            settingsViewModel.updateItem(SettingItem.ORDER_PLUGIN_SETTINGS, it)
        }
    }
}
