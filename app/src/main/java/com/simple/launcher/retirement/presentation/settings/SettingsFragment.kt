package com.simple.launcher.retirement.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentSettingsBinding
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import kotlinx.coroutines.launch

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory()
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.rvSettings.layoutManager = GridLayoutManager(requireContext(), 2)
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

        viewModel.items.attachAdapter().observe(this) { (items, adapters) ->
            binding.rvSettings.submitListAndAwait(items, adapters, true)
        }

        SettingsEventBus.events.observe(this) { item ->
            handleSettingItemClick(item)
        }
    }

    private fun handleSettingItemClick(item: SettingItem) {
        when (item.id) {
            SettingItem.ID_PIN -> {
                // Dùng suspend Pin.verify(): nếu có PIN sẵn thì xác thực trước,
                // nếu chưa có thì đi thẳng vào setup
                viewLifecycleOwner.lifecycleScope.launch {
                    if (requirePin()) {
                        sendDeeplink("app://pin_setup", extras = mapOf("addToBackStack" to true))
                    }
                }
            }
            SettingItem.ID_APP_LIST -> {
                sendDeeplink("app://app_list", extras = mapOf("addToBackStack" to true))
            }
            SettingItem.ID_CONTACT_LIST -> {
                sendDeeplink("app://contact_list", extras = mapOf("addToBackStack" to true))
            }
            SettingItem.ID_DEFAULT_LAUNCHER -> {
                sendDeeplink("app://default_launcher_setup") // Example, adjust as needed
            }
            SettingItem.ID_CLEAN_FILES -> {
                sendDeeplink("app://clean_files", extras = mapOf("addToBackStack" to true))
            }
            SettingItem.ID_CLEAN_MEMORY -> {
                sendDeeplink("app://clean_memory", extras = mapOf("addToBackStack" to true))
            }
        }
    }
}

@Deeplink
class SettingsDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = "app://settings"

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingsFragment())
        
        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }
        
        transaction.commit()
        return true
    }
}
