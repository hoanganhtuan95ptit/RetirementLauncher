package com.simple.launcher.retirement.presentation.pin_setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentPinSetupBinding
import com.simple.launcher.retirement.domain.usecase.CheckPinUseCase
import com.simple.launcher.retirement.domain.usecase.HasPinUseCase
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.text.toRich

class PinSetupFragment : Fragment() {

    private var _binding: FragmentPinSetupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PinSetupViewModel by viewModels {
        PinSetupViewModelFactory(
            HasPinUseCase.instance,
            CheckPinUseCase.instance,
            SavePinUseCase.instance
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
                    binding.tvInstruction.setText("Nhập mã PIN hiện tại".toRich())
                    binding.btnNext.setText("Xác nhận".toRich())
                }
                PinSetupViewModel.State.ENTER_NEW_PIN -> {
                    binding.tvInstruction.setText("Nhập mã PIN mới (6 chữ số)".toRich())
                    binding.btnNext.setText("Tiếp tục".toRich())
                }
                PinSetupViewModel.State.CONFIRM_NEW_PIN -> {
                    binding.tvInstruction.setText("Xác nhận mã PIN mới".toRich())
                    binding.btnNext.setText("Lưu".toRich())
                }
                PinSetupViewModel.State.SUCCESS -> {
                    Toast.makeText(context, "Thiết lập mã PIN thành công", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.tvError.setText(error?.toRich())
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

@Deeplink
class PinSetupDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = "app://pin_setup"

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PinSetupFragment())
        
        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }
        
        transaction.commit()
        return true
    }
}
