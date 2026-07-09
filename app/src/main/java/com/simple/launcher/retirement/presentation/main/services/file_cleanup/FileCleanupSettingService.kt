package com.simple.launcher.retirement.presentation.main.services.file_cleanup

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.services.FragmentCreatedService
import com.simple.launcher.retirement.utils.services.launchCollect
import kotlinx.coroutines.flow.filterIsInstance

// Legacy toggle service này đang tắt vì Settings mới đã tách group theo service/view model khác.
//@AutoRegister(apis = [SettingsFragment::class])
class FileCleanupSettingService : FragmentCreatedService {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var fileCleanupSettingViewModel: FileCleanupSettingViewModel

    override fun setup(fragment: Fragment) {

        settingsViewModel = fragment.viewModels<SettingsViewModel>().value
        fileCleanupSettingViewModel = fragment.viewModels<FileCleanupSettingViewModel>().value

        // Cập nhật UI theo trạng thái cấu hình
        fileCleanupSettingViewModel.items.launchCollect(fragment) { items ->
            settingsViewModel.updateItem(SettingItem.ORDER_TOGGLE_CLEANUP, items)
        }

        // Lắng nghe cấu hình on/off → re-evaluate BackgroundService
        PreferenceRepository.instance.isFileCleanupEnabledFlow().launchCollect(fragment) {
            if (it) (fragment.activity as? MainActivity)?.startBackgroundService()
        }

        // Xử lý sự kiện toggle từ người dùng
        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(fragment) { event ->
            val item = event.item
            if (item.id == SettingItem.ID_TOGGLE_CLEANUP) {
                handleToggle(item)
                // Đồng bộ UI với trạng thái thực từ repository
                fileCleanupSettingViewModel.refresh()
            }
        }
    }

    private suspend fun handleToggle(item: SettingItem) {

        val repository = PreferenceRepository.instance
        val isTurningOn = item.isChecked

        if (isTurningOn && !PermissionManager.requireFilePermission()) {
            return
        }

        if (!isTurningOn && !PermissionManager.requirePinPermissions()) {
            // User huỷ PIN → revert (refresh sẽ xử lý sau khi hàm này return)
            return
        }

        // Chỉ set preference — Flow bên trên sẽ tự start/stop service
        repository.setFileCleanupEnabled(isTurningOn)
    }
}
