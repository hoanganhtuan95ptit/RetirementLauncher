package com.simple.launcher.retirement.presentation.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.simple.launcher.retirement.databinding.BottomSheetCallPermissionBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment

class CallBlockPermissionBottomSheet(private val onResult: () -> Unit) : BaseBottomSheetDialogFragment<BottomSheetCallPermissionBinding>() {

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

        binding.btnGrant.setOnClickListener {
            val permissions = arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.READ_CONTACTS
            )
            requestPermissionLauncher.launch(permissions)
        }

        binding.btnSkip.setOnClickListener {
            dismiss()
            onResult()
        }
    }

    companion object {
        const val TAG = "CallBlockPermissionBottomSheet"
    }
}
