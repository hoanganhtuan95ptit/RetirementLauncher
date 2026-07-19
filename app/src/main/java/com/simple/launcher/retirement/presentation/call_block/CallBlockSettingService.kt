package com.simple.launcher.retirement.presentation.call_block

import android.os.Build
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.presentation.installer_cleanup.InstallerCleanupSettingViewModel
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.protect.ProtectSettingService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [SettingsFragment::class])
class CallBlockSettingService : ProtectSettingService() {

    private lateinit var viewModel: CallBlockSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        super.setup(settingsFragment)

        viewModel = settingsFragment.viewModels<CallBlockSettingViewModel>().value

        observeCallBlockSettingItem(settingsFragment)
        observeCallBlockSettingClick(settingsFragment)
    }

    private fun observeCallBlockSettingItem(settingsFragment: SettingsFragment) {

        viewModel.viewItemList.launchCollect(settingsFragment) {

            protectSettingViewModel.updateItem(it)
        }
    }

    private fun observeCallBlockSettingClick(settingsFragment: SettingsFragment) {

        AppEventBus.events
            .filterIsInstance<AppEvent.SettingClicked>()
            .launchCollect(settingsFragment.viewLifecycleOwner) { event ->

                setCallBlockEnabledIfToggle(event.item)
            }
    }

    private fun setCallBlockEnabledIfToggle(item: SettingItem) {

        if (item.id != SettingItem.ID_TOGGLE_CALL_BLOCK) {

            return
        }

        viewModel.setCallBlockEnabled(!item.isChecked)
    }
}
