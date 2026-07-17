package com.simple.launcher.retirement.presentation.emergency

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.simple.auto.register.AutoRegister
import com.simple.component.service.FragmentCreatedService
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

@AutoRegister(apis = [SettingsFragment::class])
class EmergencyService : FragmentCreatedService {


    override fun setup(fragment: Fragment) {

        fragment.lifecycleScope.launch {

            val pending = PreferenceRepository.instance.getPendingEmergencyCallEnabled()
            if (pending != null) {
                setEmergencyCallEnabled(pending)
            }
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(fragment) { event ->

            val item = event.item
            val isTurningOn = !item.isChecked

            if (item.id != SettingItem.ID_EMERGENCY_CALL_TOGGLE) {
                return@launchCollect
            }

            PreferenceRepository.instance.setPendingEmergencyCallEnabled(isTurningOn)

            setEmergencyCallEnabled(isTurningOn)
        }
    }

    private suspend fun setEmergencyCallEnabled(isTurningOn: Boolean) {

        try {

            if (isTurningOn) {

                if (!PermissionManager.requireEmergencyCallIntro()) {
                    return
                }

                if (!PermissionManager.requireCallPermission()) {
                    return
                }

                if (!PermissionManager.requireEmergencyContact()) {
                    return
                }

                if (!PermissionManager.requireDefaultLauncher()) {
                    return
                }
            }

            if (!isTurningOn && !PermissionManager.requirePinPermissions()) {
                return
            }

            PreferenceRepository.instance.setEmergencyCallEnabled(isTurningOn)
        } finally {

            PreferenceRepository.instance.setPendingEmergencyCallEnabled(null)
        }
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
    }
}
