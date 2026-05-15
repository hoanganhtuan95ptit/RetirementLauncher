package com.simple.launcher.retirement.presentation.pin_setup

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
import com.simple.launcher.retirement.databinding.FragmentPinSetupBinding
import com.simple.launcher.retirement.domain.usecase.CheckPinUseCase
import com.simple.launcher.retirement.domain.usecase.HasPinUseCase
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.launcher.retirement.utils.lifecycle.observe
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PinSetupFragment : BaseFragment<FragmentPinSetupBinding>() {

    private val viewModel: PinSetupViewModel by viewModels {
        PinSetupViewModelFactory(
            HasPinUseCase.instance,
            CheckPinUseCase.instance,
            SavePinUseCase.instance
        )
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPinSetupBinding {
        return FragmentPinSetupBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNext.root.setOnSafeClickListener {
            val pin = binding.etPin.text.toString()
            viewModel.handlePinInput(pin)
        }
    }

    override fun observeData() {
        super.observeData()

        viewModel.background.observe(this) { background ->
            binding.root.setBackground(background)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.etPin.text.clear()
            when (state) {
                PinSetupViewModel.State.ENTER_OLD_PIN -> {
                    binding.tvInstruction.setText("Nhập mã PIN hiện tại".toRich())
                }
                PinSetupViewModel.State.ENTER_NEW_PIN -> {
                    binding.tvInstruction.setText("Nhập mã PIN mới (6 chữ số)".toRich())
                }
                PinSetupViewModel.State.CONFIRM_NEW_PIN -> {
                    binding.tvInstruction.setText("Xác nhận mã PIN mới".toRich())
                }
                PinSetupViewModel.State.SUCCESS -> {
                    Toast.makeText(context, "Thiết lập mã PIN thành công", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.tvError.setText(error?.toRich())
        }

        viewModel.toolbar.observe(this) { state ->
            binding.toolbar.tvTitle.setText(state.title)
            val backIcon = state.backIcon
            if (backIcon != null) {
                binding.toolbar.ivLeft.visibility = View.VISIBLE
                binding.toolbar.ivLeft.setImage(backIcon)
            } else {
                binding.toolbar.ivLeft.visibility = View.GONE
            }
        }

        viewModel.action.observe(this) { state ->
            binding.btnNext.tvAction.setText(state.text)
            binding.btnNext.tvAction.setBackground(state.background)
        }
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
