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
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentAppListBinding
import com.simple.launcher.retirement.domain.usecase.GetSelectableAppsUseCase
import com.simple.launcher.retirement.domain.usecase.SaveSelectedAppsUseCase
import com.simple.launcher.retirement.presentation.default_launcher.DefaultLauncherBottomSheet
import com.simple.launcher.retirement.presentation.permissions.BlockPermissionBottomSheet
import com.simple.launcher.retirement.presentation.permissions.FilePermissionBottomSheet

import com.simple.launcher.retirement.presentation.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppListFragment : BaseFragment<FragmentAppListBinding>() {

    private val viewModel: AppListViewModel by viewModels {
        AppListViewModelFactory(GetSelectableAppsUseCase.instance, SaveSelectedAppsUseCase.instance)
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAppListBinding {
        return FragmentAppListBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            checkPermissionsAndSave()
        }

        viewModel.loadApps()
    }

    override fun observeData() {
        super.observeData()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.asFlow().attachAdapter().collectLatest { (items, adapters) ->
                binding.rvAppList.submitListAndAwait(items, adapters, true)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            AppListEventBus.events.collectLatest { entity ->
                viewModel.updateItem(entity)
            }
        }
    }

    private fun checkPermissionsAndSave() {
        if (!hasFilePermission()) {
            FilePermissionBottomSheet {
                // Sau khi xử lý xong quyền file, check tiếp quyền block
                checkBlockPermissions()
            }.show(childFragmentManager, FilePermissionBottomSheet.TAG)
            return
        }
        checkBlockPermissions()
    }

    private fun checkBlockPermissions() {
        if (!hasUsageStatsPermission() || !hasOverlayPermission()) {
            BlockPermissionBottomSheet {
                // Khi quay lại, kiểm tra lại xem đã đủ CẢ HAI chưa
                if (hasUsageStatsPermission() && hasOverlayPermission()) {
                    checkDefaultLauncher()
                } else {
                    // Nếu vẫn thiếu, yêu cầu lại (BottomSheet sẽ mở màn hình cài đặt còn thiếu)
                    Toast.makeText(context, "Bạn cần cấp đủ cả 2 quyền để tính năng hoạt động", Toast.LENGTH_SHORT).show()
                }
            }.show(childFragmentManager, BlockPermissionBottomSheet.TAG)
            return
        }
        checkDefaultLauncher()
    }

    private fun checkDefaultLauncher() {
        if (!isDefaultLauncher()) {
            DefaultLauncherBottomSheet {
                // Cuối cùng mới lưu
                saveAndExit()
            }.show(childFragmentManager, DefaultLauncherBottomSheet.TAG)
            return
        }
        saveAndExit()
    }

    private fun saveAndExit() {
        viewModel.saveSelection()
        Toast.makeText(context, R.string.app_list_saved, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun hasFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // Đơn giản hóa cho các bản cũ
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            requireContext().packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(requireContext())
    }

    private fun isDefaultLauncher(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        } else {
            // Check đơn giản cho bản cũ
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = requireContext().packageManager.resolveActivity(intent, 0)
            resolveInfo?.activityInfo?.packageName == requireContext().packageName
        }
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
