package com.simple.launcher.retirement.presentation.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentSettingsBinding
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.default_launcher.DefaultLauncherBottomSheet
import com.simple.launcher.retirement.presentation.permissions.CallBlockPermissionBottomSheet
import com.simple.launcher.retirement.presentation.pin_setup.PinVerifyBottomSheet
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.string.asStringRes
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(AppRepository.instance)
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
        val repository = AppRepository.instance
        when (item.id) {
            SettingItem.ID_PIN -> {
                PinVerifyBottomSheet {
                    sendDeeplink("app://pin_setup", extras = mapOf("addToBackStack" to true))
                }.show(childFragmentManager, PinVerifyBottomSheet.TAG)
            }
            SettingItem.ID_APP_LIST -> {
                sendDeeplink("app://app_list", extras = mapOf("addToBackStack" to true))
            }
            SettingItem.ID_CONTACT_LIST -> {
                sendDeeplink("app://contact_list", extras = mapOf("addToBackStack" to true))
            }
            SettingItem.ID_DEFAULT_LAUNCHER -> {
                DefaultLauncherBottomSheet().show(childFragmentManager, DefaultLauncherBottomSheet.TAG)
            }
            SettingItem.ID_CLEAN_FILES -> {
                sendDeeplink("app://clean_files", extras = mapOf("addToBackStack" to true))
            }
            SettingItem.ID_CLEAN_MEMORY -> {
                sendDeeplink("app://clean_memory", extras = mapOf("addToBackStack" to true))
            }
            SettingItem.ID_TOGGLE_BLOCK -> {
                handleToggleAction(item) {
                    repository.setAppBlockEnabled(item.isChecked)
                    if (item.isChecked) {
                        (activity as? com.simple.launcher.retirement.presentation.main.MainActivity)?.startAppMonitoringService()
                    } else {
                        val intent = Intent(requireContext(), com.simple.launcher.retirement.presentation.worker.AppMonitoringService::class.java)
                        requireContext().stopService(intent)
                    }
                }
            }
            SettingItem.ID_TOGGLE_CLEANUP -> {
                handleToggleAction(item) {
                    repository.setFileCleanupEnabled(item.isChecked)
                    if (item.isChecked) {
                        (activity as? com.simple.launcher.retirement.presentation.main.MainActivity)?.startFileWatcherService()
                    } else {
                        val intent = Intent(requireContext(), com.simple.launcher.retirement.presentation.worker.FileWatcherService::class.java)
                        requireContext().stopService(intent)
                    }
                }
            }
            SettingItem.ID_TOGGLE_CALL_BLOCK -> {
                if (item.isChecked && !hasCallBlockPermissions()) {
                    CallBlockPermissionBottomSheet {
                        if (hasCallBlockPermissions()) {
                            handleToggleAction(item) {
                                repository.setCallBlockEnabled(item.isChecked)
                            }
                        } else {
                            item.isChecked = false
                            viewModel.updateItem(item)
                        }
                    }.show(childFragmentManager, CallBlockPermissionBottomSheet.TAG)
                } else {
                    handleToggleAction(item) {
                        repository.setCallBlockEnabled(item.isChecked)
                    }
                }
            }
        }
    }

    private fun hasCallBlockPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }
        
        return permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun handleToggleAction(item: SettingItem, action: () -> Unit) {
        val repository = AppRepository.instance
        val isTurningOn = item.isChecked
        
        if (isTurningOn) {
            // Khi bật: không cần mã PIN
            action()
            viewModel.updateItem(item)
        } else {
            // Khi tắt: yêu cầu mã PIN
            if (!repository.hasPin()) {
                Toast.makeText(requireContext(), R.string.setting_pin_required.asStringRes(), Toast.LENGTH_LONG).show()
                item.isChecked = true // Hoàn trả trạng thái ON
                viewModel.updateItem(item)
                
                sendDeeplink("app://pin_setup", extras = mapOf("addToBackStack" to true))
            } else {
                // Hoàn trả trạng thái ON tạm thời, chỉ tắt khi verify thành công
                item.isChecked = true
                viewModel.updateItem(item)

                PinVerifyBottomSheet {
                    item.isChecked = false // Xác nhận tắt
                    action()
                    viewModel.updateItem(item)
                }.show(childFragmentManager, PinVerifyBottomSheet.TAG)
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
