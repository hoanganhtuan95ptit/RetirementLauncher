package com.simple.launcher.retirement.presentation.permissions.call

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.databinding.BottomSheetCallPermissionBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.ui.precompute.text.setText

class CallPermissionBottomSheet : BaseBottomSheetDialogFragment<BottomSheetCallPermissionBinding, CallPermissionViewModel>() {

    override val viewModel: CallPermissionViewModel by viewModels()

    private var permissionGranted = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetCallPermissionBinding {
        return BottomSheetCallPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.btnGrant.root.setOnSafeClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    override fun observeData() {
        super.observeData()

        viewModel.title.observe(this) {
            val binding = binding ?: return@observe
            binding.tvTitle.setText(it)
        }

        viewModel.description.observe(this) {
            val binding = binding ?: return@observe
            binding.tvDescription.setText(it)
        }

        viewModel.action.observe(this) { state ->
            val binding = binding ?: return@observe
            binding.btnGrant.tvAction.setText(state.text)
            binding.btnGrant.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!permissionGranted) {
            AppEventBus.post(AppEvent.PermissionCancel)
        }
    }

    companion object {
        const val TAG = "CallPermissionBottomSheet"
    }
}

@Deeplink
class CallPermissionDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PERMISSION_CALL

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        CallPermissionBottomSheet().show(fragmentActivity.supportFragmentManager, CallPermissionBottomSheet.TAG)

        return true
    }
}
