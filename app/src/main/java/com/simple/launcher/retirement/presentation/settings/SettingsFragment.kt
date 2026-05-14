package com.simple.launcher.retirement.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.databinding.FragmentSettingsBinding
import com.simple.launcher.retirement.presentation.default_launcher.DefaultLauncherBottomSheet
import com.simple.launcher.retirement.presentation.pin_setup.PinVerifyBottomSheet
import com.simple.launcher.retirement.presentation.permissions.CallBlockPermissionBottomSheet
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

import com.simple.launcher.retirement.presentation.base.BaseFragment

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.rvSettings.layoutManager = GridLayoutManager(requireContext(), 2)
        
        val repository = AppRepository.instance
        
        val settingsItems = mutableListOf<SettingItem>()
        if (repository.hasPin()) {
            settingsItems.add(SettingItem(SettingItem.ID_PIN, getString(R.string.setting_pin), android.R.drawable.ic_lock_idle_lock))
        }
        
        settingsItems.addAll(listOf(
            SettingItem(SettingItem.ID_APP_LIST, getString(R.string.setting_app_list), android.R.drawable.ic_menu_agenda),
            SettingItem(SettingItem.ID_DEFAULT_LAUNCHER, getString(R.string.setting_default_launcher), android.R.drawable.ic_menu_manage),
            SettingItem(SettingItem.ID_CONTACT_LIST, getString(R.string.setting_contact_list), android.R.drawable.ic_menu_call),
            SettingItem(SettingItem.ID_CLEAN_FILES, getString(R.string.setting_clean_files), android.R.drawable.ic_menu_delete),
            SettingItem(SettingItem.ID_CLEAN_MEMORY, getString(R.string.setting_clean_memory), android.R.drawable.ic_media_play),
            SettingItem(SettingItem.ID_TOGGLE_BLOCK, "Giám sát ứng dụng", android.R.drawable.ic_lock_lock, true, repository.isAppBlockEnabled()),
            SettingItem(SettingItem.ID_TOGGLE_CLEANUP, "Tự động xóa APK", android.R.drawable.ic_menu_save, true, repository.isFileCleanupEnabled())
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            settingsItems.add(SettingItem(SettingItem.ID_TOGGLE_CALL_BLOCK, "Chặn cuộc gọi lạ", android.R.drawable.ic_menu_call, true, repository.isCallBlockEnabled()))
        }

        binding.rvSettings.adapter = SettingsAdapter(settingsItems) { item ->
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
                    handleToggleAction(repository, item) {
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
                    handleToggleAction(repository, item) {
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
                                handleToggleAction(repository, item) {
                                    repository.setCallBlockEnabled(item.isChecked)
                                }
                            } else {
                                item.isChecked = false
                                (binding.rvSettings.adapter as? SettingsAdapter)?.notifyDataSetChanged()
                            }
                        }.show(childFragmentManager, CallBlockPermissionBottomSheet.TAG)
                    } else {
                        handleToggleAction(repository, item) {
                            repository.setCallBlockEnabled(item.isChecked)
                        }
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

    private fun handleToggleAction(repository: AppRepository, item: SettingItem, action: () -> Unit) {
        val isTurningOn = item.isChecked
        
        if (isTurningOn) {
            // Khi bật: không cần mã PIN
            action()
            (binding.rvSettings.adapter as? SettingsAdapter)?.notifyDataSetChanged()
        } else {
            // Khi tắt: yêu cầu mã PIN
            if (!repository.hasPin()) {
                Toast.makeText(requireContext(), "Bạn cần thiết lập mã PIN trước khi tắt tính năng này", Toast.LENGTH_LONG).show()
                item.isChecked = true // Hoàn trả trạng thái ON
                (binding.rvSettings.adapter as? SettingsAdapter)?.notifyDataSetChanged()
                
                sendDeeplink("app://pin_setup", extras = mapOf("addToBackStack" to true))
            } else {
                // Hoàn trả trạng thái ON tạm thời, chỉ tắt khi verify thành công
                item.isChecked = true
                (binding.rvSettings.adapter as? SettingsAdapter)?.notifyDataSetChanged()

                PinVerifyBottomSheet {
                    item.isChecked = false // Xác nhận tắt
                    action()
                    (binding.rvSettings.adapter as? SettingsAdapter)?.notifyDataSetChanged()
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
