package com.simple.launcher.retirement.presentation.emergency

import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.protect.ProtectSettingService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [ActivityCreatedService::class])
class EmergencyService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        val viewModel = fragmentActivity.viewModels<EmergencySettingViewModel>().value

        viewModel.emergencyCallEnabledFlow.launchCollect(fragmentActivity) { isEnabled ->

            if (isEnabled) {

                (fragmentActivity as? MainActivity)?.startBackgroundService()
            }
        }
    }
}

@AutoRegister(apis = [SettingsFragment::class])
class EmergencySettingService : ProtectSettingService() {

    private lateinit var viewModel: EmergencySettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {

        super.setup(settingsFragment)

        viewModel = settingsFragment.activityViewModels<EmergencySettingViewModel>().value
        viewModel.refreshStatus()

        observeEmergencySettingItem(settingsFragment)
        observeEmergencySettingClick(settingsFragment)
    }

    private fun observeEmergencySettingItem(settingsFragment: SettingsFragment) {

        viewModel.viewItemList.launchCollect(settingsFragment.viewLifecycleOwner) {

            protectSettingViewModel.updateItem(it)
        }
    }

    private fun observeEmergencySettingClick(settingsFragment: SettingsFragment) {

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(settingsFragment.viewLifecycleOwner) { event ->

            openSosSettingsIfEmergencyToggle(event.item)
        }
    }

    private suspend fun openSosSettingsIfEmergencyToggle(item: SettingItem) {

        if (item.id != SettingItem.ID_EMERGENCY_CALL_TOGGLE) {

            return
        }

        // Item emergency trên màn Protect chỉ là lối vào màn cấu hình SOS chi tiết.
        if (PermissionManager.requireEmergencyCallIntro()) {

            sendDeeplinkWithBackStack(DeepLinks.SOS_SETTINGS)
        }
    }
}
