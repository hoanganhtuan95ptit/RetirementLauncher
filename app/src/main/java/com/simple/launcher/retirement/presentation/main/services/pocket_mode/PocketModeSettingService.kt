package com.simple.launcher.retirement.presentation.main.services.pocket_mode

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.component.service.FragmentCreatedService
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.lifecycle.observe
import kotlinx.coroutines.flow.filterIsInstance

// Legacy toggle service này đang tắt vì Settings mới đã tách group theo service/view model khác.
//@AutoRegister(apis = [SettingsFragment::class])
class PocketModeSettingService : FragmentCreatedService {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var pocketModeSettingViewModel: PocketModeSettingViewModel

    override fun setup(fragment: Fragment) {

        settingsViewModel = fragment.viewModels<SettingsViewModel>().value
        pocketModeSettingViewModel = fragment.viewModels<PocketModeSettingViewModel>().value

        pocketModeSettingViewModel.items.observe(fragment) { items ->
            settingsViewModel.updateItem(SettingItem.ORDER_TOGGLE_POCKET_MODE, items)
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().observe(fragment) { event ->
            val item = event.item
            if (item.id == SettingItem.ID_TOGGLE_POCKET_MODE) {
                val isTurningOn = item.isChecked

                // Tắt tính năng yêu cầu xác thực PIN
                if (!isTurningOn && !PermissionManager.requirePinPermissions()) {
                    // Revert toggle về ON vì user huỷ nhập PIN
                    pocketModeSettingViewModel.refresh()
                    return@observe
                }

                PreferenceRepository.instance.setPocketModeEnabled(isTurningOn)
                // Đồng bộ UI sau khi thay đổi trạng thái
                pocketModeSettingViewModel.refresh()
            }
        }
    }
}
