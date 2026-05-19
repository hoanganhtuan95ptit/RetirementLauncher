package com.simple.launcher.retirement.presentation.pin_setup

import android.content.DialogInterface
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
            binding.vPin1, binding.vPin2, binding.vPin3,
            binding.vPin4, binding.vPin5, binding.vPin6
        )
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetPinVerifyBinding {
        return BottomSheetPinVerifyBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)
        setupNumpad()
    }

    private fun setupNumpad() {
        val repository = PreferenceRepository.instance

        val digitKeys = mapOf(
            binding.btnKey0 to "0",
            binding.btnKey1 to "1",
            binding.btnKey2 to "2",
            binding.btnKey3 to "3",
            binding.btnKey4 to "4",
            binding.btnKey5 to "5",
            binding.btnKey6 to "6",
            binding.btnKey7 to "7",
            binding.btnKey8 to "8",
            binding.btnKey9 to "9"
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
                            Pin.PinEventBus.post(Pin.PinVerifySuccess)
                            dismiss()
                        } else {
                            Toast.makeText(context, R.string.pin_error_incorrect, Toast.LENGTH_SHORT).show()
                            resetPin()
                        }
                    }
                }
            }
        }

        binding.btnKeyDelete.setOnClickListener {
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
            Pin.PinEventBus.post(Pin.PinCancel)
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
