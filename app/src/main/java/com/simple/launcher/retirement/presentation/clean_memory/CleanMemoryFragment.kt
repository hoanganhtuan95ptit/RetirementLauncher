package com.simple.launcher.retirement.presentation.clean_memory

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentCleanMemoryBinding
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class CleanMemoryFragment : BaseFragment<FragmentCleanMemoryBinding>() {

    private val viewModel: CleanMemoryViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCleanMemoryBinding =
        FragmentCleanMemoryBinding.inflate(inflater, container, false)

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnBoost.setOnSafeClickListener {
            viewModel.startBoost()
        }
    }

    override fun observeData() {
        super.observeData()

        viewModel.background.observe(this) { background ->
            binding.root.setBackground(background)
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

        viewModel.ramUpdate.observe(this) { update ->
            update ?: return@observe
            binding.memoryGauge.setPercent(update.info.percent, animate = update.animate)
            binding.tvPercent.text = "${update.info.percentInt}%"
            binding.tvRamDetail.text = update.info.detail
            binding.tvStatUsed.text = update.info.usedGB
            binding.tvStatFree.text = update.info.freeGB
            binding.tvStatTotal.text = update.info.totalGB
        }

        viewModel.action.observe(this) { state ->
            binding.tvBtnLabel.setText(state.text)
        }

        viewModel.boostState.observe(this) { state ->
            applyBoostState(state)
        }

        viewModel.resultTitle.observe(this) { richText ->
            if (richText != null) {
                binding.cardResult.visibility = View.VISIBLE
                binding.tvResultTitle.setText(richText)
            } else {
                binding.cardResult.visibility = View.GONE
            }
        }

        viewModel.toastEvent.observe(this) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyBoostState(state: BoostState) {
        when (state) {
            BoostState.IDLE -> {
                binding.btnBoost.setBackgroundResource(R.drawable.bg_memory_boost_btn)
                binding.tvBtnLabel.setTextColor(Color.WHITE)
                binding.ivBtnIcon.visibility = View.VISIBLE
                binding.btnBoost.isEnabled = true
            }
            BoostState.BOOSTING -> {
                binding.btnBoost.setBackgroundResource(R.drawable.bg_memory_btn_running)
                binding.tvBtnLabel.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.memory_btn_running_text)
                )
                binding.ivBtnIcon.visibility = View.GONE
                binding.btnBoost.isEnabled = false
            }
            BoostState.DONE -> {
                binding.btnBoost.setBackgroundResource(R.drawable.bg_memory_btn_done)
                binding.tvBtnLabel.setTextColor(Color.WHITE)
                binding.ivBtnIcon.visibility = View.GONE
                binding.btnBoost.isEnabled = true
            }
        }
    }
}

@Deeplink
class CleanMemoryDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = DeepLinks.CLEAN_MEMORY

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CleanMemoryFragment())

        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        return true
    }
}
