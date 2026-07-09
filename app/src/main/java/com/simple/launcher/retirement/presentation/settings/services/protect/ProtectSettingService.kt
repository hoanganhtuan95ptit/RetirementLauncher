package com.simple.launcher.retirement.presentation.settings.services.protect

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.services.SettingService
import com.simple.launcher.retirement.utils.services.launchCollect
import kotlinx.coroutines.flow.filterNotNull

@AutoRegister(apis = [SettingsFragment::class])
class ProtectSettingService : SettingService() {

    override fun setup(settingsFragment: SettingsFragment) {

        val viewModel = settingsFragment.viewModels<ProtectSettingViewModel>().value

        viewModel.viewItemList.filterNotNull().launchCollect(settingsFragment.viewLifecycleOwner) {

            settingsViewModel.updateItem(it)
        }
    }
}