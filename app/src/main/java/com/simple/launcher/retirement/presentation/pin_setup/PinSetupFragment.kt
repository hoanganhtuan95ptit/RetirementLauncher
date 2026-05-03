package com.simple.launcher.retirement.presentation.pin_setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.domain.usecase.CheckPinUseCase
import com.simple.launcher.retirement.domain.usecase.HasPinUseCase
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase

class PinSetupFragment : Fragment() {

    private val viewModel: PinSetupViewModel by viewModels {
        val repository = AppRepositoryImpl(requireContext())
        PinSetupViewModelFactory(
            HasPinUseCase(repository),
            CheckPinUseCase(repository),
            SavePinUseCase(repository)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pin_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val tvInstruction = view.findViewById<TextView>(R.id.tvInstruction)
        val etPin = view.findViewById<EditText>(R.id.etPin)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnNext = view.findViewById<Button>(R.id.btnNext)

        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            etPin.text.clear()
            when (state) {
                PinSetupViewModel.State.ENTER_OLD_PIN -> {
                    tvInstruction.text = "Nhập mã PIN hiện tại"
                    btnNext.text = "Xác nhận"
                }
                PinSetupViewModel.State.ENTER_NEW_PIN -> {
                    tvInstruction.text = "Nhập mã PIN mới (6 chữ số)"
                    btnNext.text = "Tiếp tục"
                }
                PinSetupViewModel.State.CONFIRM_NEW_PIN -> {
                    tvInstruction.text = "Xác nhận mã PIN mới"
                    btnNext.text = "Lưu"
                }
                PinSetupViewModel.State.SUCCESS -> {
                    Toast.makeText(context, "Thiết lập mã PIN thành công", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            tvError.text = error
        }

        btnNext.setOnClickListener {
            val pin = etPin.text.toString()
            viewModel.handlePinInput(pin)
        }
    }
}
