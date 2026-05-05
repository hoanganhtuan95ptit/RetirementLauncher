package com.simple.launcher.retirement.presentation.pin_setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.databinding.FragmentPinSetupBinding
import com.simple.launcher.retirement.domain.usecase.CheckPinUseCase
import com.simple.launcher.retirement.domain.usecase.HasPinUseCase
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase

class PinSetupFragment : Fragment() {

    private var _binding: FragmentPinSetupBinding? = null
    private val binding get() = _binding!!

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
    ): View {
        _binding = FragmentPinSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.etPin.text.clear()
            when (state) {
                PinSetupViewModel.State.ENTER_OLD_PIN -> {
                    binding.tvInstruction.text = "Nhập mã PIN hiện tại"
                    binding.btnNext.text = "Xác nhận"
                }
                PinSetupViewModel.State.ENTER_NEW_PIN -> {
                    binding.tvInstruction.text = "Nhập mã PIN mới (6 chữ số)"
                    binding.btnNext.text = "Tiếp tục"
                }
                PinSetupViewModel.State.CONFIRM_NEW_PIN -> {
                    binding.tvInstruction.text = "Xác nhận mã PIN mới"
                    binding.btnNext.text = "Lưu"
                }
                PinSetupViewModel.State.SUCCESS -> {
                    Toast.makeText(context, "Thiết lập mã PIN thành công", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.tvError.text = error
        }

        binding.btnNext.setOnClickListener {
            val pin = binding.etPin.text.toString()
            viewModel.handlePinInput(pin)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
