package com.simple.launcher.retirement.presentation.clock.configs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.BottomSheetClockSettingBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.ui.precompute.text.setText

class ClockSettingBottomSheet : BaseBottomSheetDialogFragment<BottomSheetClockSettingBinding, ClockSettingBottomSheetViewModel>() {

    override val viewModel: ClockSettingBottomSheetViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetClockSettingBinding {
        return BottomSheetClockSettingBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.cl12h.setOnSafeClickListener {
            viewModel.toggle24HourFormat(false)
        }

        binding.cl24h.setOnSafeClickListener {
            viewModel.toggle24HourFormat(true)
        }

        binding.swAmPm.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleAmPmEnabled(isChecked)
        }

        binding.swSolar.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleSolarCalendarEnabled(isChecked)
        }

        binding.swLunar.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleLunarCalendarEnabled(isChecked)
        }

        binding.btnCancel.setOnSafeClickListener {
            dismiss()
        }

        binding.btnSave.setOnSafeClickListener {
            viewModel.onSave()
            dismiss()
        }
    }

    override fun observeData() {
        super.observeData()

        viewModel.title.observe(this) {
            binding?.tvTitle?.setText(it)
        }

        viewModel.is24HourFormat.observe(this) { is24Hour ->
            val binding = binding ?: return@observe
            
            binding.cl12h.isSelected = !is24Hour
            binding.ivRadio12h.setImageResource(if (!is24Hour) R.drawable.ic_radio_selected else R.drawable.ic_radio_unselected)
            
            binding.cl24h.isSelected = is24Hour
            binding.ivRadio24h.setImageResource(if (is24Hour) R.drawable.ic_radio_selected else R.drawable.ic_radio_unselected)

            // AM/PM only enabled in 12h mode
            binding.swAmPm.isEnabled = !is24Hour
            binding.tvAmPmTitle.alpha = if (is24Hour) 0.5f else 1.0f
            binding.tvAmPmDesc.alpha = if (is24Hour) 0.5f else 1.0f
            binding.ivAmPm.alpha = if (is24Hour) 0.5f else 1.0f
        }

        viewModel.isAmPmEnabled.observe(this) { enabled ->
            binding?.swAmPm?.isChecked = enabled
        }

        viewModel.isSolarCalendarEnabled.observe(this) { enabled ->
            binding?.swSolar?.isChecked = enabled
        }

        viewModel.isLunarCalendarEnabled.observe(this) { enabled ->
            binding?.swLunar?.isChecked = enabled
        }
    }

    companion object {
        const val TAG = "ClockSettingBottomSheet"
    }
}

@Deeplink
class ClockSettingDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.CLOCK_SETTING

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {
        ClockSettingBottomSheet().show(fragmentActivity.supportFragmentManager, ClockSettingBottomSheet.TAG)
        return true
    }
}
