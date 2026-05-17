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
import com.simple.launcher.retirement.databinding.BottomSheetPinVerifyBinding
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class PinVerifyBottomSheet : BaseBottomSheetDialogFragment<BottomSheetPinVerifyBinding, PinVerifyViewModel>() {

    override val viewModel: PinVerifyViewModel by viewModels()

    // Tránh double-post: khi PIN đúng thì dismiss() sẽ gọi onDismiss → không post Cancel nữa
    private var pinVerified = false

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetPinVerifyBinding {
        return BottomSheetPinVerifyBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val repository = PreferenceRepository.instance

        binding.btnVerify.root.setOnSafeClickListener {

            val inputPin = binding.etPin.text.toString()
            val savedPin = repository.getPin()

            if (inputPin == savedPin) {
                pinVerified = true
                Pin.PinEventBus.post(Pin.PinVerifySuccess)
                dismiss()
            } else {

                Toast.makeText(context, "Mã PIN không chính xác", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun observeData() {
        super.observeData()
        viewModel.action.observe(this) { state ->
            binding.btnVerify.tvAction.setText(state.text)
            binding.btnVerify.tvAction.setBackground(state.background)
        }
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

    override val deeplink: String = "app://pin_verify"

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        PinVerifyBottomSheet().show(fragmentActivity.supportFragmentManager, PinVerifyBottomSheet.TAG)

        return true
    }
}