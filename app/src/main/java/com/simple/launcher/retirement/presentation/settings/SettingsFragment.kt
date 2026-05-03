package com.simple.launcher.retirement.presentation.settings

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
import com.simple.launcher.retirement.presentation.app_list.AppListFragment
import com.simple.launcher.retirement.presentation.clean_files.CleanFilesFragment
import com.simple.launcher.retirement.presentation.clean_memory.CleanMemoryFragment
import com.simple.launcher.retirement.presentation.default_launcher.DefaultLauncherBottomSheet
import com.simple.launcher.retirement.presentation.pin_setup.PinSetupFragment

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
        
        val settingsItems = listOf(
            SettingItem(SettingItem.ID_APP_LIST, "Thiết lập danh sách app", android.R.drawable.ic_menu_agenda),
            SettingItem(SettingItem.ID_DEFAULT_LAUNCHER, "Thiết lập launcher mặc định", android.R.drawable.ic_menu_manage),
            SettingItem(SettingItem.ID_CONTACT_LIST, "Thiết lập liên hệ nhanh", android.R.drawable.ic_menu_call),
            SettingItem(SettingItem.ID_CLEAN_FILES, "Dọn dẹp file lạ", android.R.drawable.ic_menu_delete),
            SettingItem(SettingItem.ID_CLEAN_MEMORY, "Giải phóng bộ nhớ", android.R.drawable.ic_media_play)
        )

        rvSettings.adapter = SettingsAdapter(settingsItems) { item ->
            when (item.id) {
                SettingItem.ID_PIN -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PinSetupFragment())
                        .addToBackStack(null)
                        .commit()
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
            }
        }
    }
}
