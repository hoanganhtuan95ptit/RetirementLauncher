package com.simple.launcher.retirement.presentation.default_launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simple.launcher.retirement.databinding.BottomSheetDefaultLauncherBinding

class DefaultLauncherBottomSheet(private val onResult: (() -> Unit)? = null) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDefaultLauncherBinding? = null
    private val binding get() = _binding!!

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        dismiss()
        onResult?.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDefaultLauncherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSetDefault.setOnClickListener {
            openDefaultLauncherSettings()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
            onResult?.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openDefaultLauncherSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    Toast.makeText(context, "Ứng dụng đã là launcher mặc định", Toast.LENGTH_SHORT).show()
                    dismiss()
                    onResult?.invoke()
                } else {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    startForResult.launch(intent)
                }
            } else {
                openHomeSettingsFallback()
            }
        } else {
            openHomeSettingsFallback()
        }
    }

    private fun openHomeSettingsFallback() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(Settings.ACTION_HOME_SETTINGS)
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
        startActivity(intent)
        dismiss()
        onResult?.invoke()
    }

    companion object {
        const val TAG = "DefaultLauncherBottomSheet"
    }
}
