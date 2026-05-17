package com.simple.launcher.retirement.presentation.settings

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.pin_setup.PinVerifyBottomSheet
import com.simple.launcher.retirement.utils.string.asStringRes

fun Fragment.handleSettingToggleAction(
    item: SettingItem,
    viewModel: SettingsViewModel,
    onDismissed: () -> Unit = { viewModel.updateItem(item) },
    action: () -> Unit
) {
    val repository = PreferenceRepository.instance
    val isTurningOn = item.isChecked
    
    if (isTurningOn) {
        // Khi bật: không cần mã PIN
        action()
        viewModel.updateItem(item)
    } else {
        // Khi tắt: yêu cầu mã PIN
        if (!repository.hasPin()) {
            Toast.makeText(requireContext(), R.string.setting_pin_required.asStringRes(), Toast.LENGTH_LONG).show()
            item.isChecked = true // Hoàn trả trạng thái ON
            viewModel.updateItem(item)
            
            sendDeeplink("app://pin_setup", extras = mapOf("addToBackStack" to true))
        } else {
            // Không pre-mutate — onDismissed sẽ revert về ON nếu user back mà chưa xác nhận PIN
            PinVerifyBottomSheet(
                onDismissed = onDismissed
            ) {
                // PIN đúng: thực hiện tắt
                action()
                viewModel.updateItem(item)
            }.show(childFragmentManager, PinVerifyBottomSheet.TAG)
        }
    }
}
