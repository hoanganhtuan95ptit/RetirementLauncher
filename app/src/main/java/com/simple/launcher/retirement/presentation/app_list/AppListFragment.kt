package com.simple.launcher.retirement.presentation.app_list

import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentAppListBinding
import com.simple.launcher.retirement.domain.usecase.GetSelectableAppsUseCase
import com.simple.launcher.retirement.domain.usecase.SaveSelectedAppsUseCase
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.permissions.file.FilePermissionBottomSheet
import com.simple.launcher.retirement.presentation.permissions.launcher.DefaultLauncherBottomSheet
import com.simple.launcher.retirement.presentation.permissions.overlay.OverlayPermissionBottomSheet
import com.simple.launcher.retirement.presentation.permissions.usage_stats.UsageStatsPermissionBottomSheet
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class AppListFragment : BaseFragment<FragmentAppListBinding>() {

    private val viewModel: AppListViewModel by viewModels {
        AppListViewModelFactory(GetSelectableAppsUseCase.instance, SaveSelectedAppsUseCase.instance)
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAppListBinding {
        return FragmentAppListBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.layoutSearch.etSearch.doAfterTextChanged {
            val text = it?.toString() ?: ""
            viewModel.search(text)
            binding.layoutSearch.ivClear.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.layoutSearch.ivClear.setOnSafeClickListener {
            binding.layoutSearch.etSearch.text = null
        }

        binding.btnSave.root.setOnSafeClickListener {
            checkPermissionsAndSave()
        }

        viewModel.loadApps()
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

        viewModel.searchState.observe(this) { state ->
            binding.layoutSearch.root.setBackground(state.background)
            binding.layoutSearch.etSearch.hint = state.hint
            binding.layoutSearch.etSearch.setHintTextColor(state.hintColor)
            binding.layoutSearch.etSearch.setTextColor(state.textColor)
            state.clearIcon?.let { binding.layoutSearch.ivClear.setImage(it) }
        }

        viewModel.saveAction.observe(this) { state ->
            binding.btnSave.tvAction.setText(state.text)
            binding.btnSave.tvAction.setBackground(state.background)
        }

        viewModel.items.attachAdapter().observe(this) { (items, adapters) ->
            binding.rvAppList.submitListAndAwait(items, adapters, true)
        }

        AppListEventBus.events.observe(this) { entity ->
            viewModel.updateItem(entity)
        }
    }

    private fun checkPermissionsAndSave() {
        val context = requireContext()
        if (!PermissionManager.hasFilePermission(context)) {
            FilePermissionBottomSheet {
                // Sau khi xử lý xong quyền file, check tiếp quyền block
                checkBlockPermissions()
            }.show(childFragmentManager, FilePermissionBottomSheet.TAG)
            return
        }
        checkBlockPermissions()
    }

    private fun checkBlockPermissions() {
        val context = requireContext()
        if (!PermissionManager.hasUsageStatsPermission(context)) {
            UsageStatsPermissionBottomSheet {
                checkBlockPermissions()
            }.show(childFragmentManager, UsageStatsPermissionBottomSheet.TAG)
            return
        }
        if (!PermissionManager.hasOverlayPermission(context)) {
            OverlayPermissionBottomSheet {
                checkBlockPermissions()
            }.show(childFragmentManager, OverlayPermissionBottomSheet.TAG)
            return
        }
        checkDefaultLauncher()
    }

    private fun checkDefaultLauncher() {
        val context = requireContext()
        if (!PermissionManager.isDefaultLauncher(context)) {
            viewModel.saveSelection()
            DefaultLauncherBottomSheet {
                onSaveSuccess()
            }.show(childFragmentManager, DefaultLauncherBottomSheet.TAG)
            return
        }
        saveAndExit()
    }

    private fun saveAndExit() {
        viewModel.saveSelection()
        onSaveSuccess()
    }

    private fun onSaveSuccess() {
        Toast.makeText(context, R.string.app_list_saved, Toast.LENGTH_SHORT).show()
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}

@Deeplink
class AppListDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = "app://app_list"

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AppListFragment())
        
        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }
        
        transaction.commit()
        return true
    }
}
