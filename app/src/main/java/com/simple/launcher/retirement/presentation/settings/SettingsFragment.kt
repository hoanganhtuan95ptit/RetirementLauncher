package com.simple.launcher.retirement.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.presentation.app_list.AppListFragment
import com.simple.launcher.retirement.presentation.clean_files.CleanFilesFragment
import com.simple.launcher.retirement.presentation.clean_memory.CleanMemoryFragment
import com.simple.launcher.retirement.presentation.default_launcher.DefaultLauncherBottomSheet
import com.simple.launcher.retirement.presentation.pin_setup.PinSetupFragment
import com.simple.launcher.retirement.presentation.pin_setup.PinVerifyBottomSheet

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rvSettings = view.findViewById<RecyclerView>(R.id.rvSettings)
        rvSettings.layoutManager = GridLayoutManager(requireContext(), 2)
        
        val repository = AppRepositoryImpl(requireContext())
        
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

        rvSettings.adapter = SettingsAdapter(settingsItems) { item ->
            when (item.id) {
                SettingItem.ID_PIN -> {
                    PinVerifyBottomSheet {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, PinSetupFragment())
                            .addToBackStack(null)
                            .commit()
                    }.show(childFragmentManager, PinVerifyBottomSheet.TAG)
                }
                SettingItem.ID_APP_LIST -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AppListFragment())
                        .addToBackStack(null)
                        .commit()
                }
                SettingItem.ID_CONTACT_LIST -> {
                    // Navigate to ContactListFragment
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, com.simple.launcher.retirement.presentation.contact_list.ContactListFragment())
                        .addToBackStack(null)
                        .commit()
                }
                SettingItem.ID_DEFAULT_LAUNCHER -> {
                    DefaultLauncherBottomSheet().show(childFragmentManager, DefaultLauncherBottomSheet.TAG)
                }
                SettingItem.ID_CLEAN_FILES -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CleanFilesFragment())
                        .addToBackStack(null)
                        .commit()
                }
                SettingItem.ID_CLEAN_MEMORY -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CleanMemoryFragment())
                        .addToBackStack(null)
                        .commit()
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
            }
        }
    }

    private fun handleToggleAction(repository: AppRepositoryImpl, item: SettingItem, action: () -> Unit) {
        val isTurningOn = item.isChecked
        
        if (isTurningOn) {
            // Khi bật: không cần mã PIN
            action()
            (view?.findViewById<RecyclerView>(R.id.rvSettings)?.adapter as? SettingsAdapter)?.notifyDataSetChanged()
        } else {
            // Khi tắt: yêu cầu mã PIN
            if (!repository.hasPin()) {
                Toast.makeText(requireContext(), "Bạn cần thiết lập mã PIN trước khi tắt tính năng này", Toast.LENGTH_LONG).show()
                item.isChecked = true // Hoàn trả trạng thái ON
                (view?.findViewById<RecyclerView>(R.id.rvSettings)?.adapter as? SettingsAdapter)?.notifyDataSetChanged()
                
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PinSetupFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                // Hoàn trả trạng thái ON tạm thời, chỉ tắt khi verify thành công
                item.isChecked = true
                (view?.findViewById<RecyclerView>(R.id.rvSettings)?.adapter as? SettingsAdapter)?.notifyDataSetChanged()

                PinVerifyBottomSheet {
                    item.isChecked = false // Xác nhận tắt
                    action()
                    (view?.findViewById<RecyclerView>(R.id.rvSettings)?.adapter as? SettingsAdapter)?.notifyDataSetChanged()
                }.show(childFragmentManager, PinVerifyBottomSheet.TAG)
            }
        }
    }
}
