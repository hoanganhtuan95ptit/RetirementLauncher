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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simple.launcher.retirement.databinding.BottomSheetBlockPermissionBinding

class BlockPermissionBottomSheet(private val onResult: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBlockPermissionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBlockPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
