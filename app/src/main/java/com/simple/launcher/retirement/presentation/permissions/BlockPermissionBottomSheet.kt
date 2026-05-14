package com.simple.launcher.retirement.presentation.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.simple.launcher.retirement.databinding.BottomSheetBlockPermissionBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment

class BlockPermissionBottomSheet(private val onResult: () -> Unit) : BaseBottomSheetDialogFragment<BottomSheetBlockPermissionBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetBlockPermissionBinding {
        return BottomSheetBlockPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.btnGrant.setOnClickListener {
            requestBlockPermissions()
            dismiss()
            onResult()
        }

        binding.btnSkip.setOnClickListener {
            dismiss()
            onResult()
        }
    }

    private fun requestBlockPermissions() {
        val context = requireContext()
        if (!hasUsageStatsPermission()) {
            // Xin quyền Usage Stats trước
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            Toast.makeText(context, "Vui lòng tìm và bật 'Retirement Launcher'", Toast.LENGTH_LONG).show()
        } else if (!Settings.canDrawOverlays(context)) {
            // Sau đó xin quyền Overlay
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            startActivity(intent)
            Toast.makeText(context, "Vui lòng cho phép ứng dụng hiển thị trên các ứng dụng khác", Toast.LENGTH_LONG).show()
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

    companion object {
        const val TAG = "BlockPermissionBottomSheet"
    }
}
