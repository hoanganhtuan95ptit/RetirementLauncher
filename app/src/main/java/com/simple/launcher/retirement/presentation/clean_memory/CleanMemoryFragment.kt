package com.simple.launcher.retirement.presentation.clean_memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentCleanMemoryBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.delay

/*class CleanMemoryFragment : BaseFragment<FragmentCleanMemoryBinding>() {

    private val viewModel: CleanMemoryViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCleanMemoryBinding {
        return FragmentCleanMemoryBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnBoost.ivAction.isVisible = true
        binding.btnBoost.tvAction.updatePadding(top = DP.DP_24, bottom = DP.DP_24)
        binding.btnBoost.root.setOnSafeClickListener {
            viewModel.startBoost()
        }
    }

    override fun observeData() {
        super.observeData()
        with(viewModel) {
            background.observe(this@CleanMemoryFragment) { background ->
                val binding = binding ?: return@observe
                binding.root.setBackground(background)
            }

            toolbar.observe(this@CleanMemoryFragment) { state ->
                val binding = binding ?: return@observe

                binding.toolbar.tvTitle.setText(state.title)

                val backIcon = state.backIcon
                if (backIcon != null) {
                    binding.toolbar.ivLeft.isVisible = true
                    binding.toolbar.ivLeft.setImage(backIcon)
                } else {
                    binding.toolbar.ivLeft.isVisible = false
                }
            }

            action.observe(this@CleanMemoryFragment) { state ->
                val binding = binding ?: return@observe

                binding.btnBoost.tvAction.setText(state.text)
                binding.btnBoost.ivAction.setImage(state.image)
                binding.btnBoost.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
            }

            boostState.observe(this@CleanMemoryFragment) {
                val binding = binding ?: return@observe

                if (it !is CleanMemoryViewModel.BoostState.Done) return@observe

                delay(1000)

                binding.animationView.isVisible = true
                binding.animationView.playAnimation()
            }

            loadingViewData.observe(this@CleanMemoryFragment) {
                val binding = binding ?: return@observe

                if (it.loading) binding.memoryGauge.startSpinning()
                else binding.memoryGauge.setPercent(it.percent, animate = true)
            }

            screenViewData.observe(this@CleanMemoryFragment) {
                val binding = binding ?: return@observe

                TransitionManager.beginDelayedTransition(binding.frameContent, AutoTransition())

                bindingRam(it.ramViewData)
                bindingRing(it.ringViewData)
                bindingResult(it.resultViewData)
            }
        }
    }

    private fun bindingRam(ramViewData: CleanMemoryViewModel.RamViewData) {
        val binding = binding ?: return
        binding.tvStatUsed.setText(ramViewData.usedBigText)
        binding.tvStatUsed.setBackground(ramViewData.usedBackground)

        binding.tvStatFree.setText(ramViewData.freedBigText)
        binding.tvStatFree.setBackground(ramViewData.freedBackground)

        binding.tvStatTotal.setText(ramViewData.totalBigText)
        binding.tvStatTotal.setBackground(ramViewData.totalBackground)
    }

    private fun bindingRing(ringViewData: CleanMemoryViewModel.RingViewData) {
        val binding = binding ?: return
        binding.tvPercent.setText(ringViewData.value)
    }

    private fun bindingResult(resultViewData: CleanMemoryViewModel.ResultViewData) {
        val binding = binding ?: return
        binding.tvResult.setText(resultViewData.text)
        binding.ivResult.setImage(resultViewData.image)

        binding.tvResult.parent.asObjectOrNull<View>()?.isVisible = resultViewData.show
        binding.tvResult.parent.asObjectOrNull<View>()?.setBackground(resultViewData.background)
    }
}*/

/*
@Deeplink
class CleanMemoryDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.CLEAN_MEMORY

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CleanMemoryFragment())

        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        return true
    }
}
*/
