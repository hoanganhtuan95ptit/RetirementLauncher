package com.simple.launcher.retirement.presentation.call_block

import android.os.Build
import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.protect.ProtectSettingService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [SettingsFragment::class])
class CallBlockSettingService : ProtectSettingService() {

    private lateinit var viewModel: CallBlockSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        super.setup(settingsFragment)

        viewModel = settingsFragment.viewModels<CallBlockSettingViewModel>().value

        viewModel.viewItemList.launchCollect(settingsFragment) {

            protectSettingViewModel.updateItem(it)
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(settingsFragment.viewLifecycleOwner) { event ->

            val item = event.item
            val isTurningOn = !item.isChecked

            if (item.id != SettingItem.ID_TOGGLE_CALL_BLOCK) {
                return@launchCollect
            }

            if (isTurningOn) {

                if (!PermissionManager.requireCallBlockIntro()) {
                    return@launchCollect
                }

                if (!PermissionManager.requireCallBlockPermissions()) {
                    return@launchCollect
                }
            }

            if (!isTurningOn && !PermissionManager.requirePinPermissions()) {
                return@launchCollect
            }

            PreferenceRepository.instance.setCallBlockEnabled(isTurningOn)
        }
    }
}
