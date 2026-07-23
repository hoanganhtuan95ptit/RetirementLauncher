package com.simple.launcher.retirement.presentation.settings.services.optimization

import androidx.fragment.app.viewModels
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.services.SettingService
import com.simple.launcher.retirement.utils.lifecycle.observe
import kotlinx.coroutines.flow.filterNotNull

//@AutoRegister(apis = [SettingsFragment::class])
class OptimizationSettingService : SettingService() {

    override fun setup(settingsFragment: SettingsFragment) {

        val viewModel = settingsFragment.viewModels<OptimizationSettingViewModel>().value

        viewModel.viewItemList.filterNotNull().observe(settingsFragment.viewLifecycleOwner) {

            settingsViewModel.updateItem(it)
        }
    }
}
