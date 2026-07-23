package com.simple.launcher.retirement.presentation.settings.services.protect

import androidx.annotation.CallSuper
import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.services.SettingService
import com.simple.launcher.retirement.utils.lifecycle.observe

@AutoRegister(apis = [SettingsFragment::class])
open class ProtectSettingService : SettingService() {

    protected lateinit var protectSettingViewModel: ProtectSettingViewModel

    @CallSuper
    override fun setup(settingsFragment: SettingsFragment) {

        protectSettingViewModel = settingsFragment.viewModels<ProtectSettingViewModel>().value

        protectSettingViewModel.viewItemList.observe(settingsFragment.viewLifecycleOwner) {

            settingsViewModel.updateItem(1, it)
        }
    }
}
