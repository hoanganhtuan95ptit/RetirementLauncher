package com.simple.launcher.retirement.presentation.pin_setup

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.BottomSheetPinVerifyBinding
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText

class PinVerifyBottomSheet : BaseBottomSheetDialogFragment<BottomSheetPinVerifyBinding, PinVerifyViewModel>() {

    override val viewModel: PinVerifyViewModel by viewModels()

    // Tránh double-post: khi PIN đúng thì dismiss() sẽ gọi onDismiss → không post Cancel nữa
    private var pinVerified = false

    private val pinBuilder = StringBuilder()
    private val PIN_LENGTH = 6

    private val pinDots: List<View> by lazy {
        listOf(
            binding.layoutPinDots.vPin1, binding.layoutPinDots.vPin2, binding.layoutPinDots.vPin3,
            binding.layoutPinDots.vPin4, binding.layoutPinDots.vPin5, binding.layoutPinDots.vPin6
        )
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetPinVerifyBinding {
        return BottomSheetPinVerifyBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)
        setupNumpad()
    }

    override fun observeData() {
        super.observeData()
        viewModel.content.observe(this) { state ->
            binding.tvTitle.setText(state.title)
            binding.tvDesc.setText(state.desc)
        }

        viewModel.numpadState.observe(this) { state ->
            val digitKeys = mapOf(
                binding.layoutNumpad.btnKey0 to "0",
                binding.layoutNumpad.btnKey1 to "1",
                binding.layoutNumpad.btnKey2 to "2",
                binding.layoutNumpad.btnKey3 to "3",
                binding.layoutNumpad.btnKey4 to "4",
                binding.layoutNumpad.btnKey5 to "5",
                binding.layoutNumpad.btnKey6 to "6",
                binding.layoutNumpad.btnKey7 to "7",
                binding.layoutNumpad.btnKey8 to "8",
                binding.layoutNumpad.btnKey9 to "9"
            )

            digitKeys.forEach { (btn, text) ->
                btn.text = text
                btn.textSize = state.textSize
                btn.setTextColor(state.textColor)
                btn.backgroundTintList = ColorStateList.valueOf(state.rippleColor)
            }

            binding.layoutNumpad.btnKeyDelete.imageTintList = ColorStateList.valueOf(state.deleteIconColor)
            binding.layoutNumpad.btnKeyDelete.backgroundTintList = ColorStateList.valueOf(state.rippleColor)
        }
    }

    private fun setupNumpad() {
        val repository = PreferenceRepository.instance

        val digitKeys = mapOf(
            binding.layoutNumpad.btnKey0 to "0",
            binding.layoutNumpad.btnKey1 to "1",
            binding.layoutNumpad.btnKey2 to "2",
            binding.layoutNumpad.btnKey3 to "3",
            binding.layoutNumpad.btnKey4 to "4",
            binding.layoutNumpad.btnKey5 to "5",
            binding.layoutNumpad.btnKey6 to "6",
            binding.layoutNumpad.btnKey7 to "7",
            binding.layoutNumpad.btnKey8 to "8",
            binding.layoutNumpad.btnKey9 to "9"
        )

        for ((btn, digit) in digitKeys) {
            btn.setOnClickListener {
                if (pinBuilder.length < PIN_LENGTH) {
                    pinBuilder.append(digit)
                    updatePinDots()
                    if (pinBuilder.length == PIN_LENGTH) {
                        val savedPin = repository.getPin()
                        if (pinBuilder.toString() == savedPin) {
                            pinVerified = true
                            AppEventBus.post(AppEvent.PinVerifySuccess)
                            dismiss()
                        } else {
                            Toast.makeText(context, R.string.pin_error_incorrect, Toast.LENGTH_SHORT).show()
                            resetPin()
                        }
                    }
                }
            }
        }

        binding.layoutNumpad.btnKeyDelete.setOnClickListener {
            if (pinBuilder.isNotEmpty()) {
                pinBuilder.deleteCharAt(pinBuilder.length - 1)
                updatePinDots()
            }
        }
    }

    private fun updatePinDots() {
        val filled = pinBuilder.length
        pinDots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < filled) R.drawable.bg_pin_dot_filled
                else R.drawable.bg_pin_dot_empty
            )
        }
    }

    private fun resetPin() {
        pinBuilder.clear()
        updatePinDots()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Chỉ post Cancel khi người dùng thực sự huỷ (không phải sau khi xác thực thành công)
        if (!pinVerified) {
            AppEventBus.post(AppEvent.PinCancel)
        }
    }

    companion object {
        const val TAG = "PinVerifyBottomSheet"
    }
}

@Deeplink
class PinVerifyDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PIN_VERIFY

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        PinVerifyBottomSheet().show(fragmentActivity.supportFragmentManager, PinVerifyBottomSheet.TAG)

        return true
    }
}
