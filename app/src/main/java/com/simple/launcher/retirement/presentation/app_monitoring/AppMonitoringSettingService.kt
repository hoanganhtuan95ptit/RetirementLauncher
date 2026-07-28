package com.simple.launcher.retirement.presentation.app_monitoring

import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.protect.ProtectSettingService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.observe
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [ActivityCreatedService::class])
class AppMonitoringService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        val viewModel = fragmentActivity.viewModels<AppMonitoringSettingViewModel>().value

        viewModel.appBlockEnabledFlow.observe(fragmentActivity) {

            if (it) (fragmentActivity as? MainActivity)?.startBackgroundService()
        }
    }
}

@AutoRegister(apis = [SettingsFragment::class])
class AppMonitoringSettingService : ProtectSettingService() {

    private lateinit var viewModel: AppMonitoringSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {
        super.setup(settingsFragment)

        viewModel = settingsFragment.activityViewModels<AppMonitoringSettingViewModel>().value
        viewModel.refreshStatus()

        observeAppMonitoringSettingItem(settingsFragment)
        observeAppMonitoringSettingClick(settingsFragment)
    }

    private fun observeAppMonitoringSettingItem(settingsFragment: SettingsFragment) {

        viewModel.viewItemList.observe(settingsFragment) {

            protectSettingViewModel.updateItem(it)
        }
    }

    private fun observeAppMonitoringSettingClick(settingsFragment: SettingsFragment) {

        AppEventBus.events
            .filterIsInstance<AppEvent.SettingClicked>()
            .observe(settingsFragment.viewLifecycleOwner) { event ->

                setAppBlockEnabledIfToggle(event.item)
            }
    }

    private fun setAppBlockEnabledIfToggle(item: SettingItem) {

        if (item.id != SettingItem.ID_TOGGLE_BLOCK) {

            return
        }

        viewModel.setAppBlockEnabled(!item.isChecked)
    }
}
