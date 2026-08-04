package com.simple.launcher.retirement.presentation.settings.services.protect

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.observe
import kotlinx.coroutines.flow.filterIsInstance

@AutoRegister(apis = [SettingsFragment::class])
class NotificationBlockSettingService : ProtectSettingService() {

    private lateinit var viewModel: NotificationBlockSettingViewModel

    override fun setup(settingsFragment: SettingsFragment) {

        super.setup(settingsFragment)

        viewModel = settingsFragment.viewModels<NotificationBlockSettingViewModel>().value

        viewModel.viewItemList.observe(settingsFragment) {

            protectSettingViewModel.updateItem(it)
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>()
            .observe(settingsFragment.viewLifecycleOwner) { event ->

                if (event.item.id == SettingItem.ID_NOTIFICATION_BLOCK) {

                    sendDeeplinkWithBackStack(DeepLinks.NOTIFICATION_BLOCK)
                }
            }
    }
}
