package com.simple.launcher.retirement.presentation.installer_cleanup

import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.presentation.app_monitoring.AppMonitoringSettingViewModel
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.protect.ProtectSettingService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [ActivityCreatedService::class])
class InstallerCleanupService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        val viewModel = fragmentActivity.viewModels<InstallerCleanupSettingViewModel>().value

        viewModel.fileCleanupEnabledFlow.launchCollect(fragmentActivity) {

            if (it) (fragmentActivity as? MainActivity)?.startBackgroundService()
        }
    }
}

@AutoRegister(apis = [SettingsFragment::class])
class InstallerCleanupSettingService : ProtectSettingService() {

    private lateinit var viewModel: InstallerCleanupSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {

        super.setup(settingsFragment)

        viewModel = settingsFragment.activityViewModels<InstallerCleanupSettingViewModel>().value

        observeInstallerCleanupSettingItem(settingsFragment)
        observeInstallerCleanupSettingClick(settingsFragment)
    }

    private fun observeInstallerCleanupSettingItem(settingsFragment: SettingsFragment) {

        viewModel.viewItemList.launchCollect(settingsFragment) {

            protectSettingViewModel.updateItem(it)
        }
    }

    private fun observeInstallerCleanupSettingClick(settingsFragment: SettingsFragment) {

        AppEventBus.events
            .filterIsInstance<AppEvent.SettingClicked>()
            .launchCollect(settingsFragment.viewLifecycleOwner) { event ->

                setFileCleanupEnabledIfToggle(event.item)
            }
    }

    private fun setFileCleanupEnabledIfToggle(item: SettingItem) {

        if (item.id != SettingItem.ID_TOGGLE_INSTALLER_CLEANUP) {

            return
        }

        viewModel.setFileCleanupEnabled(!item.isChecked)
    }
}
