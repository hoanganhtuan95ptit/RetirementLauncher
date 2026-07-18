package com.simple.launcher.retirement.presentation.emergency

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
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
import kotlinx.coroutines.launch

@AutoRegister(apis = [ActivityCreatedService::class])
class EmergencyService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        fragmentActivity.lifecycleScope.launch {

            val pending = PreferenceRepository.instance.getPendingEmergencyConfig()
            if (pending != null) {
                setEmergencyCallEnabled(pending)
            }
        }

        AppEventBus.events.filterIsInstance<AppEvent.SOSUpdate>().launchCollect(fragmentActivity) { event ->

            PreferenceRepository.instance.setPendingEmergencyConfig(event.config)
            if (!setEmergencyCallEnabled(event.config)) {
                AppEventBus.post(AppEvent.SOSUpdateCancel)
            }
        }
    }

    private suspend fun setEmergencyCallEnabled(
        config: com.simple.launcher.retirement.domain.model.SOSConfig
    ): Boolean {

        try {

            if (!checkPermissions(config)) {
                return false
            }

            PreferenceRepository.instance.setEmergencyTimeout(config.timeout)
            PreferenceRepository.instance.setExclusionPeriods(config.exclusionPeriods)

            PreferenceRepository.instance.setEmergencyCallEnabled(config.isEnabled)
            AppEventBus.post(AppEvent.SOSUpdateSuccess)
            return true
        } finally {

            PreferenceRepository.instance.setPendingEmergencyConfig(null)
        }
    }

    private suspend fun checkPermissions(config: com.simple.launcher.retirement.domain.model.SOSConfig): Boolean {

        if (config.isEnabled) {

            return PermissionManager.requireEmergencyCallIntro() &&
                    PermissionManager.requireCallPermission() &&
                    PermissionManager.requireEmergencyContact() &&
                    PermissionManager.requireDefaultLauncher()
        }

        return PermissionManager.requirePinPermissions()
    }
}

@AutoRegister(apis = [SettingsFragment::class])
class EmergencySettingService : ProtectSettingService() {

    private lateinit var viewModel: EmergencySettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {
        super.setup(settingsFragment)

        viewModel = settingsFragment.viewModels<EmergencySettingViewModel>().value

        viewModel.viewItemList.launchCollect(settingsFragment) {

            protectSettingViewModel.updateItem(it)
        }

        viewModel.emergencyCallEnabledFlow.launchCollect(settingsFragment.viewLifecycleOwner) {

            if (it) (settingsFragment.activity as? MainActivity)?.startBackgroundService()
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(settingsFragment.viewLifecycleOwner) { event ->

            val item = event.item

            if (item.id != SettingItem.ID_EMERGENCY_CALL_TOGGLE) {
                return@launchCollect
            }

            com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack(com.simple.launcher.retirement.presentation.DeepLinks.SOS_SETTINGS)
        }
    }
}
