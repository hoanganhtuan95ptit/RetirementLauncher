package com.simple.launcher.retirement.presentation.pin_setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.simple.launcher.retirement.databinding.BottomSheetPinVerifyBinding
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class PinVerifyBottomSheet(private val onSuccess: () -> Unit) : BaseBottomSheetDialogFragment<BottomSheetPinVerifyBinding, PinVerifyViewModel>() {

    override val viewModel: PinVerifyViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetPinVerifyBinding {
        return BottomSheetPinVerifyBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val repository = AppRepository.instance

        binding.btnVerify.root.setOnSafeClickListener {
            val inputPin = binding.etPin.text.toString()
            val savedPin = repository.getPin()

            if (inputPin == savedPin) {
                onSuccess()
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

    companion object {
        const val TAG = "PinVerifyBottomSheet"
    }
}
