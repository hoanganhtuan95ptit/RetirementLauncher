package com.simple.launcher.retirement.presentation.settings.services.protect

import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.presentation.notification_block.NotificationBlockSettingViewModel
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.observe
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [ActivityCreatedService::class])
class NotificationBlockMainService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        val viewModel = fragmentActivity.viewModels<NotificationBlockSettingViewModel>().value

        viewModel.notificationBlockEnabledFlow.observe(fragmentActivity) {

            if (it) (fragmentActivity as? MainActivity)?.startBackgroundService()
        }
    }
}

@AutoRegister(apis = [SettingsFragment::class])
class NotificationBlockSettingService : ProtectSettingService() {

    private lateinit var viewModel: NotificationBlockSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {

        super.setup(settingsFragment)

        viewModel = settingsFragment.activityViewModels<NotificationBlockSettingViewModel>().value
        viewModel.refreshStatus()

        observeNotificationBlockSettingItem(settingsFragment)
        observeNotificationBlockSettingClick(settingsFragment)
    }

    private fun observeNotificationBlockSettingItem(settingsFragment: SettingsFragment) {

        viewModel.viewItemList.observe(settingsFragment) {

            protectSettingViewModel.updateItem(it)
        }
    }

    private fun observeNotificationBlockSettingClick(settingsFragment: SettingsFragment) {

        AppEventBus.events
            .filterIsInstance<AppEvent.SettingClicked>()
            .observe(settingsFragment.viewLifecycleOwner) { event ->

                onNotificationBlockSettingClicked(event.item)
            }
    }

    private fun onNotificationBlockSettingClicked(item: SettingItem) {

        if (item.id != SettingItem.ID_NOTIFICATION_BLOCK) return

        // Nếu tính năng đã bật: mở màn hình chi tiết (danh sách app + retention).
        // Nếu chưa bật: toggle sang bật — use case sẽ dẫn user qua flow xin quyền
        // Notification Access trước khi ghi pref.
        if (item.isChecked) {

            sendDeeplinkWithBackStack(DeepLinks.NOTIFICATION_BLOCK)
        } else {

            viewModel.setNotificationBlockEnabled(true)
        }
    }
}
