package com.simple.launcher.retirement.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentSettingsBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.app_block.BlockActivity
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    val viewModel: SettingsViewModel by viewModels<SettingsViewModel>()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSettingsBinding {

        return FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.toolbar.ivLeft.setOnSafeClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {

                return (viewModel.viewItemList.value.getOrNull(position) as? SpanSizeLookupViewItem)?.getSpanSize() ?: 1
            }
        }
        binding.rvSettings.layoutManager = layoutManager
    }

    override fun observeData() = with(viewModel) {

        toolbar.observe(this@SettingsFragment) { state ->

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

        background.observe(this@SettingsFragment) { background ->

            val binding = binding ?: return@observe

            binding.root.setBackground(background)
        }

        viewItemList.attachAdapter().observe(this@SettingsFragment) { (items, adapters) ->

            val binding = binding ?: return@observe

            binding.rvSettings.submitListAndAwait(items, adapters, true)
        }

        AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().observe(this@SettingsFragment) { event ->

            handleSettingItemClick(event.item)
        }
    }

    private fun handleSettingItemClick(item: SettingItem) {

        when (item.id) {

            SettingItem.ID_PIN -> openPinSetup()
            SettingItem.ID_APP_LIST -> sendDeeplinkWithBackStack(DeepLinks.APP_LIST)
            SettingItem.ID_CONTACT_LIST -> sendDeeplinkWithBackStack(DeepLinks.CONTACT_LIST)
            SettingItem.ID_DEFAULT_LAUNCHER -> requireDefaultLauncher()
        }
    }

    private fun openPinSetup() = viewLifecycleOwner.lifecycleScope.launch {

        val hasPin = PermissionManager.hasPinPermission()
        if (PermissionManager.requirePinPermissions() && hasPin) {

            sendDeeplinkWithBackStack(DeepLinks.PIN_SETUP)
        }
    }

    private fun requireDefaultLauncher() = viewLifecycleOwner.lifecycleScope.launch {

        PermissionManager.requireDefaultLauncher()
    }
}

@Deeplink
class SettingsDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.SETTINGS

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingsFragment())

        if (extras?.get("addToBackStack") == true) {

            transaction.addToBackStack(null)
        }

        transaction.commitAllowingStateLoss()
        return true
    }
}
