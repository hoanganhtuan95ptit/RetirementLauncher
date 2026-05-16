package com.simple.launcher.retirement.presentation.permissions.call_block

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.simple.launcher.retirement.databinding.BottomSheetCallPermissionBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class CallBlockPermissionBottomSheet(private val onResult: () -> Unit) : BaseBottomSheetDialogFragment<BottomSheetCallPermissionBinding, CallBlockPermissionViewModel>() {

    override val viewModel: CallBlockPermissionViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onResult()
        dismiss()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetCallPermissionBinding {
        return BottomSheetCallPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.btnGrant.root.setOnSafeClickListener {
            requestPermissionLauncher.launch(PermissionManager.getCallBlockPermissions())
        }
    }

    override fun observeData() {
        super.observeData()
        viewModel.action.observe(this) { state ->
            binding.btnGrant.tvAction.setText(state.text)
            binding.btnGrant.tvAction.setBackground(state.background)
        }
    }

    companion object {
        const val TAG = "CallBlockPermissionBottomSheet"
    }
}
