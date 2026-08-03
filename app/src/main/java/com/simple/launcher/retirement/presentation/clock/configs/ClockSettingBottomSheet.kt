package com.simple.launcher.retirement.presentation.clock.configs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import androidx.core.view.updateLayoutParams
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.exts.dp
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

        viewModel.horizontalPadding.observe(this) { padding ->

            val binding = binding ?: return@observe
            binding.root.setPadding(padding, binding.root.paddingTop, padding, binding.root.paddingBottom)
        }

        viewModel.verticalPadding.observe(this) { padding ->

            val binding = binding ?: return@observe
            binding.root.setPadding(binding.root.paddingLeft, padding, binding.root.paddingRight, padding)
        }

        viewModel.title.observe(this) {

            binding?.tvTitle?.setText(it)
        }

        viewModel.formatHeader.observe(this) {

            val binding = binding ?: return@observe
            binding.tvFormatHeader.setText(it)
            binding.tvFormatHeader.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                topMargin = 24.dp().toInt()
            }
        }

        viewModel.twelveHourTitle.observe(this) {

            val binding = binding ?: return@observe
            binding.tv12hTitle.setText(it)
        }

        viewModel.twelveHourExample.observe(this) {

            val binding = binding ?: return@observe
            binding.tv12hExample.setText(it)
        }

        viewModel.twentyFourHourTitle.observe(this) {

            val binding = binding ?: return@observe
            binding.tv24hTitle.setText(it)
        }

        viewModel.twentyFourHourExample.observe(this) {

            val binding = binding ?: return@observe
            binding.tv24hExample.setText(it)
        }

        viewModel.amPmTitle.observe(this) {

            val binding = binding ?: return@observe
            binding.tvAmPmTitle.setText(it)
        }

        viewModel.amPmDesc.observe(this) {

            val binding = binding ?: return@observe
            binding.tvAmPmDesc.setText(it)
        }

        viewModel.solarTitle.observe(this) {

            val binding = binding ?: return@observe
            binding.tvSolarTitle.setText(it)
        }

        viewModel.solarDesc.observe(this) {

            val binding = binding ?: return@observe
            binding.tvSolarDesc.setText(it)
        }

        viewModel.lunarTitle.observe(this) {

            val binding = binding ?: return@observe
            binding.tvLunarTitle.setText(it)
        }

        viewModel.lunarDesc.observe(this) {

            val binding = binding ?: return@observe
            binding.tvLunarDesc.setText(it)
        }

        viewModel.cancelLabel.observe(this) {

            val binding = binding ?: return@observe
            binding.btnCancel.setText(it)
        }

        viewModel.saveLabel.observe(this) {

            val binding = binding ?: return@observe
            binding.btnSave.setText(it)
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

            setupLayoutMargins(binding)
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

    private fun setupLayoutMargins(binding: BottomSheetClockSettingBinding) {

        binding.cl12h.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 12.dp().toInt()
            marginEnd = 8.dp().toInt()
        }
        binding.cl12h.setPadding(16.dp().toInt(), 16.dp().toInt(), 16.dp().toInt(), 16.dp().toInt())

        binding.cl24h.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            marginStart = 8.dp().toInt()
        }
        binding.cl24h.setPadding(16.dp().toInt(), 16.dp().toInt(), 16.dp().toInt(), 16.dp().toInt())

        binding.ivRadio12h.updateLayoutParams<ViewGroup.MarginLayoutParams> { }
        binding.tv12hTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            marginStart = 12.dp().toInt()
        }
        binding.tv12hExample.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 4.dp().toInt()
        }

        binding.ivRadio24h.updateLayoutParams<ViewGroup.MarginLayoutParams> { }
        binding.tv24hTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            marginStart = 12.dp().toInt()
        }
        binding.tv24hExample.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 4.dp().toInt()
        }

        binding.divider1.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 24.dp().toInt()
        }

        binding.ivAmPm.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 16.dp().toInt()
        }
        binding.tvAmPmTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            marginStart = 16.dp().toInt()
            marginEnd = 16.dp().toInt()
        }
        binding.tvAmPmDesc.updateLayoutParams<ViewGroup.MarginLayoutParams> { }

        binding.divider2.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 16.dp().toInt()
        }

        binding.ivSolar.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 16.dp().toInt()
        }
        binding.tvSolarTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            marginStart = 16.dp().toInt()
            marginEnd = 16.dp().toInt()
        }
        binding.tvSolarDesc.updateLayoutParams<ViewGroup.MarginLayoutParams> { }

        binding.divider3.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 16.dp().toInt()
        }

        binding.ivLunar.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 16.dp().toInt()
        }
        binding.tvLunarTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            marginStart = 16.dp().toInt()
            marginEnd = 16.dp().toInt()
        }
        binding.tvLunarDesc.updateLayoutParams<ViewGroup.MarginLayoutParams> { }

        binding.frameAction.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            topMargin = 32.dp().toInt()
        }
        binding.btnCancel.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            marginEnd = 8.dp().toInt()
        }
        binding.btnSave.updateLayoutParams<ViewGroup.MarginLayoutParams> {

            marginStart = 8.dp().toInt()
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
