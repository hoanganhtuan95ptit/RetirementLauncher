package com.simple.launcher.retirement.presentation.pin.verify

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.BottomSheetPinVerifyBinding
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PinVerifyBottomSheet : BaseBottomSheetDialogFragment<BottomSheetPinVerifyBinding, PinVerifyViewModel>() {

    override val viewModel: PinVerifyViewModel by viewModels()

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

        binding.numpadView.onDigitClick = { digit ->

            appendDigit(digit)
        }

        binding.numpadView.onDeleteClick = {

            deleteLastDigit()
        }
    }

    override fun observeData() {

        super.observeData()

        viewModel.content.observe(this) { state ->

            binding.tvTitle.setText(state.title)
            binding.tvDesc.setText(state.desc)
        }
    }

    private fun appendDigit(digit: String) {

        if (pinBuilder.length >= PIN_LENGTH) {

            return
        }

        pinBuilder.append(digit)
        updatePinDots()
        verifyPinIfNeeded()
    }

    private fun deleteLastDigit() {

        if (pinBuilder.isEmpty()) {

            return
        }

        pinBuilder.deleteCharAt(pinBuilder.length - 1)
        updatePinDots()
    }

    private fun verifyPinIfNeeded() {

        if (pinBuilder.length < PIN_LENGTH) {

            return
        }

        val savedPin = PreferenceRepository.instance.getPin()
        if (pinBuilder.toString() == savedPin) {

            handlePinVerified()
            return
        }

        showInvalidPin()
        resetPin()
    }

    private fun handlePinVerified() {

        pinVerified = true
        AppEventBus.post(AppEvent.PinVerifySuccess)
        dismiss()
    }

    private fun showInvalidPin() {

        Toast.makeText(context, R.string.pin_error_incorrect, Toast.LENGTH_SHORT).show()
    }

    private fun updatePinDots() {

        val filled = pinBuilder.length
        pinDots.forEachIndexed { index, dot ->

            val background = if (index < filled) R.drawable.bg_pin_dot_filled
            else R.drawable.bg_pin_dot_empty

            dot.setBackgroundResource(background)
        }
    }

    private fun resetPin() = viewLifecycleOwner.lifecycleScope.launch {
        binding.numpadView.setIsClickable(false)
        delay(500)
        binding.numpadView.setIsClickable(true)
        pinBuilder.clear()
        updatePinDots()
    }

    override fun onDismiss(dialog: DialogInterface) {

        super.onDismiss(dialog)
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
