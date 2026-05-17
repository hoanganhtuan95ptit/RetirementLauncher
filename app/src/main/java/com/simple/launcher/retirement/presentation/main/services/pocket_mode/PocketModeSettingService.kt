package com.simple.launcher.retirement.presentation.main.services.pocket_mode

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.adapter.ViewItem
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsEventBus
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.presentation.settings.requirePin
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.services.FragmentViewCreatedService
import com.simple.launcher.retirement.utils.services.launchCollect
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@AutoRegister(apis = [SettingsFragment::class])
class PocketModeSettingService : FragmentViewCreatedService {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var pocketModeSettingViewModel: PocketModeSettingViewModel

    override fun setup(fragment: Fragment) {

        settingsViewModel = fragment.viewModels<SettingsViewModel>().value
        pocketModeSettingViewModel = fragment.viewModels<PocketModeSettingViewModel>().value

        pocketModeSettingViewModel.items.launchCollect(fragment) { items ->
            settingsViewModel.updateItem(SettingItem.ORDER_TOGGLE_POCKET_MODE, items)
        }

        SettingsEventBus.events.launchCollect(fragment) { item ->
            if (item.id == SettingItem.ID_TOGGLE_POCKET_MODE) {
                val isTurningOn = item.isChecked

                // Tắt tính năng yêu cầu xác thực PIN
                if (!isTurningOn && !requirePin()) {
                    // Revert toggle về ON vì user huỷ nhập PIN
                    pocketModeSettingViewModel.refresh()
                    return@launchCollect
                }

                PreferenceRepository.instance.setPocketModeEnabled(isTurningOn)
                // Đồng bộ UI sau khi thay đổi trạng thái
                pocketModeSettingViewModel.refresh()
            }
        }
    }

    class PocketModeSettingViewModel : BaseViewModel() {

        private val repository by lazy { PreferenceRepository.instance }

        val refreshTrigger = MutableStateFlow(0)

        val items: StateFlow<List<ViewItem>> = combineState(
            flow1 = strings,
            flow2 = themes,
            flow3 = repository.isPocketModeEnabledFlow(),
            flow4 = refreshTrigger,
            initialValue = emptyList()
        ) { stringMap, themeMap, isEnabled, _ ->

            val textColor = themeMap.getColor(android.R.attr.textColorPrimary)

            listOf(
                SettingItem(
                    SettingItem.ID_TOGGLE_POCKET_MODE,
                    stringMap.getString(R.string.setting_pocket_mode).toRich().with(ForegroundColor(textColor)),
                    ImageRes(android.R.drawable.ic_menu_compass),
                    isSwitch = true,
                    isChecked = isEnabled
                )
            )
        }

        fun refresh() = refreshTrigger.value++
    }
}
