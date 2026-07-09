package com.simple.launcher.retirement.presentation.settings.services.optimization

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.services.SettingService
import kotlinx.coroutines.flow.filterNotNull

@AutoRegister(apis = [SettingsFragment::class])
class OptimizationSettingService : SettingService() {

    override fun setup(settingsFragment: SettingsFragment) {

        val viewModel = settingsFragment.viewModels<OptimizationSettingViewModel>().value

        viewModel.viewItemList.filterNotNull().launchCollect(settingsFragment.viewLifecycleOwner) {

            settingsViewModel.updateItem(it)
        }
    }
}