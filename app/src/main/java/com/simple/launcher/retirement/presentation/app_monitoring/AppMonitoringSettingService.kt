package com.simple.launcher.retirement.presentation.app_monitoring

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.protect.ProtectSettingService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [SettingsFragment::class])
class AppMonitoringSettingService : ProtectSettingService() {

    private lateinit var viewModel: AppMonitoringSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {
        super.setup(settingsFragment)

        viewModel = settingsFragment.viewModels<AppMonitoringSettingViewModel>().value

        viewModel.viewItemList.launchCollect(settingsFragment) {

            protectSettingViewModel.updateItem(it)
        }

        viewModel.isAppBlockEnabledFlow.launchCollect(settingsFragment.viewLifecycleOwner) {

            if (it) (settingsFragment.activity as? MainActivity)?.startBackgroundService()
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(settingsFragment.viewLifecycleOwner) { event ->

            val item = event.item
            val isTurningOn = !item.isChecked

            if (item.id != SettingItem.ID_TOGGLE_BLOCK) {
                return@launchCollect
            }

            if (isTurningOn && !PermissionManager.requireUsageStatsPermission()) {
                return@launchCollect
            }

            if (!isTurningOn && !PermissionManager.requirePinPermissions()) {
                return@launchCollect
            }

            PreferenceRepository.instance.setAppBlockEnabled(isTurningOn)
        }
    }
}
