package com.simple.launcher.retirement.presentation.permissions

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simple.launcher.retirement.databinding.BottomSheetFilePermissionBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment

class FilePermissionBottomSheet(private val onResult: () -> Unit) : BaseBottomSheetDialogFragment<BottomSheetFilePermissionBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetFilePermissionBinding {
        return BottomSheetFilePermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.btnGrant.setOnClickListener {
            requestFilePermission()
            dismiss()
            onResult()
        }

        binding.btnSkip.setOnClickListener {
            dismiss()
            onResult()
        }
    }

    private fun requestFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        } else {
            // Với Android < 11, quyền này thường được xin lúc runtime bình thường, 
            // nhưng ở đây ta đơn giản hóa bằng cách mở cài đặt ứng dụng
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${requireContext().packageName}")
            startActivity(intent)
        }
    }

    companion object {
        const val TAG = "FilePermissionBottomSheet"
    }
}
