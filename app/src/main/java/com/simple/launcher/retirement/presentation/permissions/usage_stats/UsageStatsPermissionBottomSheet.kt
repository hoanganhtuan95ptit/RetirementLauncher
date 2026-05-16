package com.simple.launcher.retirement.presentation.permissions.usage_stats

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.simple.launcher.retirement.databinding.BottomSheetUsageStatsPermissionBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class UsageStatsPermissionBottomSheet(private val onResult: () -> Unit) : BaseBottomSheetDialogFragment<BottomSheetUsageStatsPermissionBinding, UsageStatsPermissionViewModel>() {

    override val viewModel: UsageStatsPermissionViewModel by viewModels()

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (PermissionManager.hasUsageStatsPermission(requireContext())) {
            dismiss()
            onResult()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetUsageStatsPermissionBinding {
        return BottomSheetUsageStatsPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.btnGrant.root.setOnSafeClickListener {
            requestUsageStatsPermission()
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

    private fun requestUsageStatsPermission() {
        val context = requireContext()
        if (PermissionManager.hasUsageStatsPermission(context)) {
            dismiss()
            onResult()
            return
        }
        startForResult.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    companion object {
        const val TAG = "UsageStatsPermissionBottomSheet"
    }
}
