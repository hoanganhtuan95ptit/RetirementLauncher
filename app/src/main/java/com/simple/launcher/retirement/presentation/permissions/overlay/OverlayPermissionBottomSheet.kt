package com.simple.launcher.retirement.presentation.permissions.overlay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.simple.launcher.retirement.databinding.BottomSheetOverlayPermissionBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class OverlayPermissionBottomSheet(private val onResult: () -> Unit) : BaseBottomSheetDialogFragment<BottomSheetOverlayPermissionBinding, OverlayPermissionViewModel>() {

    override val viewModel: OverlayPermissionViewModel by viewModels()

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (PermissionManager.hasOverlayPermission(requireContext())) {
            dismiss()
            onResult()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetOverlayPermissionBinding {
        return BottomSheetOverlayPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.btnGrant.root.setOnSafeClickListener {
            requestOverlayPermission()
        }
    }

    override fun observeData() {
        super.observeData()
        viewModel.title.observe(this) { title ->
            binding.tvTitle.setText(title)
        }
        viewModel.description.observe(this) { description ->
            binding.tvDescription.setText(description)
        }
        viewModel.action.observe(this) { state ->
            binding.btnGrant.tvAction.setText(state.text)
            binding.btnGrant.tvAction.setBackground(state.background)
        }
    }

    private fun requestOverlayPermission() {
        val context = requireContext()
        if (PermissionManager.hasOverlayPermission(context)) {
            dismiss()
            onResult()
            return
        }
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:${context.packageName}")
        startForResult.launch(intent)
    }

    companion object {
        const val TAG = "OverlayPermissionBottomSheet"
    }
}
