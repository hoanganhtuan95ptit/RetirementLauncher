package com.simple.launcher.retirement.presentation.settings.services.common

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.services.SettingService

@AutoRegister(apis = [SettingsFragment::class])
class CommonSettingService : SettingService() {

    override fun setup(settingsFragment: SettingsFragment) {

        val viewModel = settingsFragment.viewModels<CommonSettingViewModel>().value

        viewModel.viewItemList.launchCollect(settingsFragment.viewLifecycleOwner) {

            settingsViewModel.updateItem(it)
        }
    }
}