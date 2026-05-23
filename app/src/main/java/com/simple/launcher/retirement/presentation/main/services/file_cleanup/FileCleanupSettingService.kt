package com.simple.launcher.retirement.presentation.main.services.file_cleanup

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.adapter.ViewItem
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.permissions.file.FilePermissionBottomSheet
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.presentation.settings.requirePin
import com.simple.launcher.retirement.presentation.worker.FileWatcherService
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.services.launchCollect
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.services.FragmentCreatedService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

@AutoRegister(apis = [SettingsFragment::class])
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

        // Lắng nghe cấu hình on/off từ cache → tự bật/tắt FileWatcherService
        PreferenceRepository.instance.isFileCleanupEnabledFlow().launchCollect(fragment) { isEnabled ->
            val context = fragment.requireContext()
            if (isEnabled) {
                (fragment.activity as? MainActivity)?.startFileWatcherService()
            } else {
                context.stopService(Intent(context, FileWatcherService::class.java))
            }
        }

        // Xử lý sự kiện toggle từ người dùng
        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(fragment) { event ->
            val item = event.item
            if (item.id == SettingItem.ID_TOGGLE_CLEANUP) {
                handleToggle(fragment, item)
                // Đồng bộ UI với trạng thái thực từ repository
                fileCleanupSettingViewModel.refresh()
            }
        }
    }

    private suspend fun handleToggle(fragment: Fragment, item: SettingItem) {

        val repository = PreferenceRepository.instance
        val context = fragment.requireContext()
        val isTurningOn = item.isChecked

        if (isTurningOn && !PermissionManager.hasFilePermission(context)) {
            // Xin quyền file thông qua bottom sheet (callback → suspend)
            val granted = fragment.awaitFilePermission()
            if (!granted) return
        }

        if (!isTurningOn && !requirePin()) {
            // User huỷ PIN → revert (refresh sẽ xử lý sau khi hàm này return)
            return
        }

        // Chỉ set preference — Flow bên trên sẽ tự start/stop service
        repository.setFileCleanupEnabled(isTurningOn)
    }

    /**
     * Hiển thị FilePermissionBottomSheet và chờ kết quả qua AppEventBus.
     * @return true nếu quyền được cấp, false nếu user huỷ hoặc từ chối.
     */
    private suspend fun Fragment.awaitFilePermission(): Boolean {
        FilePermissionBottomSheet().show(childFragmentManager, FilePermissionBottomSheet.TAG)
        val result = AppEventBus.events
            .filterIsInstance<AppEvent.PermissionResult>()
            .first()
        return result is AppEvent.PermissionAccept
    }

    class FileCleanupSettingViewModel : BaseViewModel() {

        private val repository = PreferenceRepository.instance

        val refreshTrigger = MutableStateFlow(0)

        val items: StateFlow<List<ViewItem>> = combineState(
            flow1 = strings,
            flow2 = themes,
            flow3 = repository.isFileCleanupEnabledFlow(),
            flow4 = refreshTrigger,
            initialValue = emptyList()
        ) { stringMap, themeMap, isEnabled, _ ->

            val textColor = themeMap.getColor(android.R.attr.textColorPrimary)

            listOf(
                SettingItem(
                    SettingItem.ID_TOGGLE_CLEANUP,
                    stringMap.getString(R.string.setting_auto_cleanup_apk).toRich().with(ForegroundColor(textColor)),
                    ImageRes(android.R.drawable.ic_menu_save),
                    isSwitch = true,
                    isChecked = isEnabled
                )
            )
        }

        fun refresh() = refreshTrigger.value++
    }
}
