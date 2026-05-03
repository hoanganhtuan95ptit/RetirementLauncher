package com.simple.launcher.retirement.presentation.pin_setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl

class PinVerifyBottomSheet(private val onSuccess: () -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_pin_verify, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etPin = view.findViewById<EditText>(R.id.etPin)
        val btnVerify = view.findViewById<Button>(R.id.btnVerify)
        val repository = AppRepositoryImpl(requireContext())

        btnVerify.setOnClickListener {
            val inputPin = etPin.text.toString()
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
