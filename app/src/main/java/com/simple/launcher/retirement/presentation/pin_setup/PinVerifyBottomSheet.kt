package com.simple.launcher.retirement.presentation.pin_setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.databinding.BottomSheetPinVerifyBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment

class PinVerifyBottomSheet(private val onSuccess: () -> Unit) : BaseBottomSheetDialogFragment<BottomSheetPinVerifyBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetPinVerifyBinding {
        return BottomSheetPinVerifyBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val repository = AppRepository.instance

        binding.btnVerify.setOnClickListener {
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

    companion object {
        const val TAG = "PinVerifyBottomSheet"
    }
}
