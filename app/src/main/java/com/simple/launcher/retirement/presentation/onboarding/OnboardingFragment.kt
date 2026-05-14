package com.simple.launcher.retirement.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStart.setOnClickListener {
            val repository = AppRepositoryImpl(requireContext())
            repository.setOnboardingCompleted(true)

            sendDeeplink("app://settings")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
