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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simple.launcher.retirement.databinding.BottomSheetFilePermissionBinding

class FilePermissionBottomSheet(private val onResult: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilePermissionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilePermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
