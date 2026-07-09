package com.simple.launcher.retirement.presentation.main.services.app_monitoring

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.adapter.ViewItem
import com.simple.auto.register.AutoRegister
import com.simple.component.service.FragmentCreatedService
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance


class AppMonitoringSettingViewModel : BaseViewModel() {

    private val repository = PreferenceRepository.instance

    val refreshTrigger = MutableStateFlow(0)

    val isAppBlockEnabledFlow = repository.isAppBlockEnabledFlow()

    val items: StateFlow<List<ViewItem>> = combineState(flow1 = resources, flow2 = refreshTrigger, flow3 = isAppBlockEnabledFlow, initialValue = emptyList()) { resources, _, isEnabled ->

        val textColor = resources.textColorPrimary

        value = SettingItem(
            id = SettingItem.ID_TOGGLE_BLOCK,
            icon = BigImage(android.R.drawable.ic_lock_lock),
            title = resources.getString(R.string.setting_app_monitoring).with(BigForegroundColor(textColor)).build(),
            isSwitch = true,
            isChecked = isEnabled
        ).let {
            listOf(it)
        }
    }

    fun refresh() = refreshTrigger.value++
}

@AutoRegister(apis = [SettingsFragment::class])
class AppMonitoringSettingService : FragmentCreatedService {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var appMonitoringSettingViewModel: AppMonitoringSettingViewModel

    override fun setup(fragment: Fragment) {

        settingsViewModel = fragment.viewModels<SettingsViewModel>().value
        appMonitoringSettingViewModel = fragment.viewModels<AppMonitoringSettingViewModel>().value

        // Cập nhật UI theo trạng thái cấu hình
        appMonitoringSettingViewModel.items.launchCollect(fragment) { items ->
            settingsViewModel.updateItem(SettingItem.ORDER_TOGGLE_BLOCK, items)
        }

        // Lắng nghe cấu hình on/off → re-evaluate BackgroundService
        PreferenceRepository.instance.isAppBlockEnabledFlow().launchCollect(fragment) {
            if (it) (fragment.activity as? MainActivity)?.startBackgroundService()
        }

        // Xử lý sự kiện toggle từ người dùng
        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(fragment) { event ->
            val item = event.item
            if (item.id == SettingItem.ID_TOGGLE_BLOCK) {
                handleToggle(item)
                appMonitoringSettingViewModel.refresh()
            }
        }
    }

    private suspend fun handleToggle(item: SettingItem) {

        val repository = PreferenceRepository.instance
        val isTurningOn = item.isChecked

        if (isTurningOn && !PermissionManager.requireUsageStatsPermission()) {
            // User huỷ cấp quyền → revert về OFF
            appMonitoringSettingViewModel.refresh()
            return
        }

        if (!isTurningOn && !PermissionManager.requirePinPermissions()) {
            // User huỷ nhập PIN → revert về ON
            appMonitoringSettingViewModel.refresh()
            return
        }

        // Chỉ set preference — Flow bên trên sẽ tự start/stop service
        repository.setAppBlockEnabled(isTurningOn)
    }
}
