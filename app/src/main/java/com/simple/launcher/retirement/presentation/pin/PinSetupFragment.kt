package com.simple.launcher.retirement.presentation.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentPinSetupBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

class PinSetupFragment : BaseFragment<FragmentPinSetupBinding>() {

    private val viewModel: PinSetupViewModel by viewModels()
    private var pinSetupCompleted = false

    private val pinBuilder = StringBuilder()
    private val PIN_LENGTH = 6

    private val pinDots: List<View> by lazy {
        val binding = binding ?: return@lazy emptyList()
        listOf(
            binding.layoutPinDots.vPin1, binding.layoutPinDots.vPin2, binding.layoutPinDots.vPin3,
            binding.layoutPinDots.vPin4, binding.layoutPinDots.vPin5, binding.layoutPinDots.vPin6
        )
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPinSetupBinding {

        return FragmentPinSetupBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.toolbar.ivLeft.setOnSafeClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.numpadView.onDigitClick = { digit ->

            appendDigit(digit)
        }

        binding.numpadView.onDeleteClick = {

            deleteLastDigit()
        }
    }

    private fun appendDigit(digit: String) {

        if (pinBuilder.length >= PIN_LENGTH) {

            return
        }

        pinBuilder.append(digit)
        updatePinDots()
        submitPinIfNeeded()
    }

    private fun deleteLastDigit() {

        if (pinBuilder.isEmpty()) {

            return
        }

        pinBuilder.deleteCharAt(pinBuilder.length - 1)
        updatePinDots()
    }

    private fun submitPinIfNeeded() {

        if (pinBuilder.length < PIN_LENGTH) {

            return
        }

        viewModel.handlePinInput(pinBuilder.toString())
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
            val binding = binding ?: return@observe

            binding.root.setBackground(background)
        }

        viewModel.instruction.observe(this) { instruction ->
            val binding = binding ?: return@observe

            binding.tvInstruction.setText(instruction)
        }

        viewModel.state.observe(this) { state ->

            resetPin()
            when (state) {
                PinSetupViewModel.State.SUCCESS -> {

                    pinSetupCompleted = true
                    Toast.makeText(context, R.string.pin_setup_success, Toast.LENGTH_SHORT).show()
                    AppEventBus.post(AppEvent.PinSetupSuccess)
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                else -> Unit
            }
        }

        viewModel.error.observe(this) { errorRes ->
            val binding = binding ?: return@observe

            binding.tvError.setText(errorRes?.let { BigText(getString(it)) })
        }

        viewModel.toolbar.observe(this) { state ->
            val binding = binding ?: return@observe

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
            val binding = binding ?: return@observe

            binding.btnNext.tvAction.setText(state.text)
            binding.btnNext.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    override fun onDestroy() {

        super.onDestroy()
        if (!pinSetupCompleted) {

            AppEventBus.post(AppEvent.PinCancel)
        }
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
