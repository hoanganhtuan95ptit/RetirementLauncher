package com.simple.launcher.retirement.presentation.main.services.app_monitoring

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.adapter.ViewItem
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.presentation.worker.AppMonitoringService
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.services.FragmentCreatedService
import com.simple.launcher.retirement.utils.services.launchCollect
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance


class AppMonitoringSettingViewModel : BaseViewModel() {

    private val repository = PreferenceRepository.instance

    val refreshTrigger = MutableStateFlow(0)

    val isAppBlockEnabledFlow = repository.isAppBlockEnabledFlow()

    val items: StateFlow<List<ViewItem>> = combineState(flow1 = strings, flow2 = themes, flow3 = refreshTrigger, flow4 = isAppBlockEnabledFlow, initialValue = emptyList()) { stringMap, themeMap, _, isEnabled ->

        val textColor = themeMap.getColor(android.R.attr.textColorPrimary)

        SettingItem(
            id = SettingItem.ID_TOGGLE_BLOCK,
            icon = ImageRes(android.R.drawable.ic_lock_lock),
            title = stringMap.getString(R.string.setting_app_monitoring).toRich().with(ForegroundColor(textColor)),
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

        // Lắng nghe cấu hình on/off từ cache → tự bật/tắt AppMonitoringService
        PreferenceRepository.instance.isAppBlockEnabledFlow().launchCollect(fragment) { isEnabled ->
            val context = fragment.requireContext()
            if (isEnabled) {
                (fragment.activity as? MainActivity)?.startAppMonitoringService()
            } else {
                context.stopService(Intent(context, AppMonitoringService::class.java))
            }
        }

        // Xử lý sự kiện toggle từ người dùng
        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(fragment) { event ->
            val item = event.item
            if (item.id == SettingItem.ID_TOGGLE_BLOCK) {
                handleToggle(fragment, item)
                appMonitoringSettingViewModel.refresh()
            }
        }
    }

    private suspend fun handleToggle(fragment: Fragment, item: SettingItem) {

        val repository = PreferenceRepository.instance
        val context = fragment.requireContext()
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
