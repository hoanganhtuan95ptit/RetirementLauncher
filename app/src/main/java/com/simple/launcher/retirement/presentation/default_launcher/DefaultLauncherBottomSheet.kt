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
import com.simple.launcher.retirement.databinding.BottomSheetDefaultLauncherBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment

class DefaultLauncherBottomSheet(private val onResult: (() -> Unit)? = null) : BaseBottomSheetDialogFragment<BottomSheetDefaultLauncherBinding>() {

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        dismiss()
        onResult?.invoke()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetDefaultLauncherBinding {
        return BottomSheetDefaultLauncherBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.btnSetDefault.setOnClickListener {
            openDefaultLauncherSettings()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
            onResult?.invoke()
        }
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
