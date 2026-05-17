package com.simple.launcher.retirement.presentation.main.services.call_block

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.adapter.ViewItem
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.permissions.call_block.CallBlockPermissionBottomSheet
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsEventBus
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.presentation.settings.requirePin
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.services.FragmentViewCreatedService
import com.simple.launcher.retirement.utils.services.launchCollect
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@AutoRegister(apis = [SettingsFragment::class])
class CallBlockSettingService : FragmentViewCreatedService {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var callBlockSettingViewModel: CallBlockSettingViewModel

    override fun setup(fragment: Fragment) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        settingsViewModel = fragment.viewModels<SettingsViewModel>().value
        callBlockSettingViewModel = fragment.viewModels<CallBlockSettingViewModel>().value

        callBlockSettingViewModel.items.launchCollect(fragment) { items ->
            settingsViewModel.updateItem(SettingItem.ORDER_TOGGLE_CALL_BLOCK, items)
        }

        SettingsEventBus.events.launchCollect(fragment) { item ->
            if (item.id == SettingItem.ID_TOGGLE_CALL_BLOCK) {
                handleToggle(fragment, item)
                // Đồng bộ UI với trạng thái thực từ repository
                callBlockSettingViewModel.refresh()
            }
        }
    }

    private suspend fun handleToggle(fragment: Fragment, item: SettingItem) {

        val repository = PreferenceRepository.instance
        val context = fragment.requireContext()
        val isTurningOn = item.isChecked

        if (isTurningOn && !PermissionManager.hasCallBlockPermissions(context)) {
            // Xin quyền call block thông qua bottom sheet (callback → suspend)
            val granted = fragment.awaitCallBlockPermission()
            if (!granted) return
        }

        if (!isTurningOn && !requirePin()) {
            // User huỷ PIN → revert (refresh sẽ xử lý sau khi hàm này return)
            return
        }

        repository.setCallBlockEnabled(isTurningOn)
    }

    /**
     * Hiển thị CallBlockPermissionBottomSheet và chờ kết quả qua AppEventBus.
     * @return true nếu quyền được cấp, false nếu user huỷ.
     */
    private suspend fun Fragment.awaitCallBlockPermission(): Boolean {
        CallBlockPermissionBottomSheet().show(childFragmentManager, CallBlockPermissionBottomSheet.TAG)
        val result = AppEventBus.events
            .filter { it is AppEventBus.PermissionResult }
            .first()
        return result is AppEventBus.PermissionAccept
    }

    class CallBlockSettingViewModel : BaseViewModel() {

        private val repository = PreferenceRepository.instance

        val refreshTrigger = MutableStateFlow(0)

        val items: StateFlow<List<ViewItem>> = combineState(
            flow1 = strings,
            flow2 = themes,
            flow3 = repository.isCallBlockEnabledFlow(),
            flow4 = refreshTrigger,
            initialValue = emptyList()
        ) { stringMap, themeMap, isEnabled, _ ->

            val textColor = themeMap.getColor(android.R.attr.textColorPrimary)

            listOf(
                SettingItem(
                    SettingItem.ID_TOGGLE_CALL_BLOCK,
                    stringMap.getString(R.string.setting_call_block).toRich().with(ForegroundColor(textColor)),
                    ImageRes(android.R.drawable.ic_menu_call),
                    isSwitch = true,
                    isChecked = isEnabled
                )
            )
        }

        fun refresh() = refreshTrigger.value++
    }
}
