package com.simple.launcher.retirement.presentation.main.services.app_monitoring

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.presentation.settings.services.SettingService
import com.simple.launcher.retirement.presentation.settings.services.settingItem
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull

@AutoRegister(apis = [SettingsFragment::class])
class AppMonitoringSettingService : SettingService() {

    private lateinit var viewModel: AppMonitoringSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {

        viewModel = settingsFragment.viewModels<AppMonitoringSettingViewModel>().value

        // Cập nhật UI theo trạng thái cấu hình
        viewModel.viewItemList.filterNotNull().launchCollect(settingsFragment) {

            settingsViewModel.updateItem(it)
        }

        // Lắng nghe cấu hình on/off → re-evaluate BackgroundService
        PreferenceRepository.instance.isAppBlockEnabledFlow().launchCollect(settingsFragment) {

            if (it) (settingsFragment.activity as? MainActivity)?.startBackgroundService()
        }

        // Xử lý sự kiện toggle từ người dùng
        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(settingsFragment) { event ->

            val item = event.item
            if (item.id == SettingItem.ID_TOGGLE_BLOCK) {
                handleToggle(item)
                viewModel.refresh()
            }
        }
    }

    private suspend fun handleToggle(item: SettingItem) {

        val repository = PreferenceRepository.instance
        val isTurningOn = item.isChecked

        if (isTurningOn && !PermissionManager.requireUsageStatsPermission()) {
            // User huỷ cấp quyền → revert về OFF
            viewModel.refresh()
            return
        }

        if (!isTurningOn && !PermissionManager.requirePinPermissions()) {
            // User huỷ nhập PIN → revert về ON
            viewModel.refresh()
            return
        }

        // Chỉ set preference — Flow bên trên sẽ tự start/stop service
        repository.setAppBlockEnabled(isTurningOn)
    }
}
