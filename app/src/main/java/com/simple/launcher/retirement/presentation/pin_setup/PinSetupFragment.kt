package com.simple.launcher.retirement.presentation.pin_setup

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentPinSetupBinding
import com.simple.launcher.retirement.domain.usecase.SavePinUseCase
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class PinSetupFragment : BaseFragment<FragmentPinSetupBinding>() {

    private val viewModel: PinSetupViewModel by viewModels {
        PinSetupViewModelFactory(SavePinUseCase.instance)
    }

    private val pinBuilder = StringBuilder()
    private val PIN_LENGTH = 6

    private val pinDots: List<View> by lazy {
        listOf(
            binding.vPin1, binding.vPin2, binding.vPin3,
            binding.vPin4, binding.vPin5, binding.vPin6
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

        setupNumpad()
    }

    private fun setupNumpad() {
        val digitKeys = mapOf(
            binding.layoutNumpad.btnKey0 to "0",
            binding.layoutNumpad.btnKey1 to "1",
            binding.layoutNumpad.btnKey2 to "2",
            binding.layoutNumpad.btnKey3 to "3",
            binding.layoutNumpad.btnKey4 to "4",
            binding.layoutNumpad.btnKey5 to "5",
            binding.layoutNumpad.btnKey6 to "6",
            binding.layoutNumpad.btnKey7 to "7",
            binding.layoutNumpad.btnKey8 to "8",
            binding.layoutNumpad.btnKey9 to "9"
        )

        for ((btn, digit) in digitKeys) {
            btn.setOnClickListener {
                if (pinBuilder.length < PIN_LENGTH) {
                    pinBuilder.append(digit)
                    updatePinDots()
                    if (pinBuilder.length == PIN_LENGTH) {
                        viewModel.handlePinInput(pinBuilder.toString())
                    }
                }
            }
        }

        binding.layoutNumpad.btnKeyDelete.setOnClickListener {
            if (pinBuilder.isNotEmpty()) {
                pinBuilder.deleteCharAt(pinBuilder.length - 1)
                updatePinDots()
            }
        }
    }

    private fun updatePinDots() {
        val filled = pinBuilder.length
        pinDots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < filled) R.drawable.bg_pin_dot_filled
                else R.drawable.bg_pin_dot_empty
            )
        }
    }

    private fun resetPin() {
        pinBuilder.clear()
        updatePinDots()
    }

    override fun observeData() {
        super.observeData()

        viewModel.background.observe(this) { background ->
            binding.root.setBackground(background)
        }

        viewModel.instruction.observe(this) { instruction ->
            binding.tvInstruction.setText(instruction)
        }

        viewModel.state.observe(this) { state ->
            resetPin()
            when (state) {
                PinSetupViewModel.State.SUCCESS -> {
                    Toast.makeText(context, R.string.pin_setup_success, Toast.LENGTH_SHORT).show()
                    AppEventBus.post(AppEvent.PinSetupSuccess)
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                else -> Unit
            }
        }

        viewModel.error.observe(this) { errorRes ->
            binding.tvError.setText(errorRes?.let { RichText(getString(it)) })
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
            binding.btnNext.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }

        viewModel.numpadState.observe(this) { state ->
            val digitKeys = mapOf(
                binding.layoutNumpad.btnKey0 to "0",
                binding.layoutNumpad.btnKey1 to "1",
                binding.layoutNumpad.btnKey2 to "2",
                binding.layoutNumpad.btnKey3 to "3",
                binding.layoutNumpad.btnKey4 to "4",
                binding.layoutNumpad.btnKey5 to "5",
                binding.layoutNumpad.btnKey6 to "6",
                binding.layoutNumpad.btnKey7 to "7",
                binding.layoutNumpad.btnKey8 to "8",
                binding.layoutNumpad.btnKey9 to "9"
            )

            digitKeys.forEach { (btn, text) ->
                btn.text = text
                btn.textSize = state.textSize
                btn.setTextColor(state.textColor)
                btn.backgroundTintList = ColorStateList.valueOf(state.rippleColor)
            }

            binding.layoutNumpad.btnKeyDelete.imageTintList = ColorStateList.valueOf(state.deleteIconColor)
            binding.layoutNumpad.btnKeyDelete.backgroundTintList = ColorStateList.valueOf(state.rippleColor)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppEventBus.post(AppEvent.PinCancel)
    }
}

@Deeplink
class PinSetupDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PIN_SETUP

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PinSetupFragment())
            .addToBackStack(null)

        transaction.commit()
        return true
    }
}
