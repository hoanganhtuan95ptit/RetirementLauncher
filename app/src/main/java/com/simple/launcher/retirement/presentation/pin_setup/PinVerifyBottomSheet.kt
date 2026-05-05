package com.simple.launcher.retirement.presentation.pin_setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.databinding.BottomSheetPinVerifyBinding

class PinVerifyBottomSheet(private val onSuccess: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPinVerifyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPinVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = AppRepositoryImpl(requireContext())

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PinVerifyBottomSheet"
    }
}
