package com.simple.launcher.retirement.presentation.main.services.call_block

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.services.FragmentCreatedService
import com.simple.launcher.retirement.utils.services.launchCollect
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance

// Legacy toggle service này đang tắt vì Settings mới đã tách group theo service/view model khác.
//@AutoRegister(apis = [SettingsFragment::class])
class CallBlockSettingService : FragmentCreatedService {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var callBlockSettingViewModel: CallBlockSettingViewModel

    override fun setup(fragment: Fragment) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        settingsViewModel = fragment.viewModels<SettingsViewModel>().value
        callBlockSettingViewModel = fragment.viewModels<CallBlockSettingViewModel>().value

        callBlockSettingViewModel.items.launchCollect(fragment) { items ->
            settingsViewModel.updateItem(SettingItem.ORDER_TOGGLE_CALL_BLOCK, items)
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(fragment) { event ->
            val item = event.item
            if (item.id == SettingItem.ID_TOGGLE_CALL_BLOCK) {
                handleToggle(item)
                // Đồng bộ UI với trạng thái thực từ repository
                callBlockSettingViewModel.refresh()
            }
        }
    }

    private suspend fun handleToggle(item: SettingItem) {

        val repository = PreferenceRepository.instance
        val isTurningOn = item.isChecked

        if (isTurningOn && !PermissionManager.requireCallBlockPermissions()) {
            return
        }

        if (!isTurningOn && !PermissionManager.requirePinPermissions()) {
            // User huỷ PIN → revert (refresh sẽ xử lý sau khi hàm này return)
            return
        }

        repository.setCallBlockEnabled(isTurningOn)
    }
}
