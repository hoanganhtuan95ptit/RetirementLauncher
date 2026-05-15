package com.simple.launcher.retirement.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.databinding.FragmentOnboardingBinding
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OnboardingFragment : BaseFragment<FragmentOnboardingBinding>() {

    private val viewModel: OnboardingViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOnboardingBinding {
        return FragmentOnboardingBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.btnStart.root.setOnSafeClickListener {
            val repository = AppRepository.instance
            repository.setOnboardingCompleted(true)

            sendDeeplink("app://settings")
        }
    }

    override fun observeData() {
        super.observeData()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.action.collectLatest { state ->
                binding.btnStart.tvAction.setText(state.text)
                binding.btnStart.tvAction.setBackground(state.background)
            }
        }
    }
}

@Deeplink
class OnboardingDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = "app://onboarding"

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OnboardingFragment())
            .commit()
        return true
    }
}
