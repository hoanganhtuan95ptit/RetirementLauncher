package com.simple.launcher.retirement.presentation.main.services.pocket_mode
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.simple.adapter.ViewItem
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.settings.SettingItem
import com.simple.launcher.retirement.presentation.settings.SettingsEventBus
import com.simple.launcher.retirement.presentation.settings.SettingsFragment
import com.simple.launcher.retirement.presentation.settings.SettingsViewModel
import com.simple.launcher.retirement.presentation.settings.handleSettingToggleAction
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.services.ActivityCreatedService
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

    override fun setup(fragment: Fragment) {
        val settingsViewModel by fragment.viewModels<SettingsViewModel>()
        val pocketModeSettingViewModel by fragment.viewModels<PocketModeSettingViewModel>()

        pocketModeSettingViewModel.items.launchCollect(fragment) { items ->
            items.forEach { item ->
                if (item is SettingItem) {
                    settingsViewModel.addItemAtEnd(item)
                }
            }
        }

        SettingsEventBus.events.launchCollect(fragment) { item ->
            if (item.id == SettingItem.ID_TOGGLE_POCKET_MODE) {
                fragment.handleSettingToggleAction(
                    item = item,
                    viewModel = settingsViewModel
                ) {
                    PreferenceRepository.instance.setPocketModeEnabled(item.isChecked)
                }
            }
        }
    }

    class PocketModeSettingViewModel : BaseViewModel() {

        private val repository by lazy {
            PreferenceRepository.instance
        }

        val items: StateFlow<List<ViewItem>> = combineState(
            flow1 = strings,
            flow2 = themes,
            flow3 = repository.isPocketModeEnabledFlow(),
            initialValue = emptyList()
        ) { stringMap, themeMap, isEnabled ->

            val textColor = themeMap.getColor(android.R.attr.textColorPrimary)
            val settingsItems = mutableListOf<ViewItem>()

            SettingItem(
                SettingItem.ID_TOGGLE_POCKET_MODE,
                stringMap.getString(R.string.setting_pocket_mode).toRich().with(ForegroundColor(textColor)),
                ImageRes(android.R.drawable.ic_menu_compass), true, isEnabled
            ).let {

                settingsItems.add(it)
            }

            settingsItems
        }
    }
}
