package com.simple.launcher.retirement.presentation.permissions.overlay

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.databinding.BottomSheetOverlayPermissionBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.ui.precompute.text.setText

class OverlayPermissionBottomSheet : BaseBottomSheetDialogFragment<BottomSheetOverlayPermissionBinding, OverlayPermissionViewModel>() {

    override val viewModel: OverlayPermissionViewModel by viewModels()

    // Tránh double-post: khi permission được grant thì dismiss() → onDismiss không post Cancel nữa
    private var permissionGranted = false

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (PermissionManager.hasOverlayPermission()) {
            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
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

        val binding = binding ?: return

        binding.btnGrant.root.setOnSafeClickListener {
            requestOverlayPermission()
        }
    }

    override fun observeData() {
        super.observeData()

        viewModel.title.observe(this) { title ->
            val binding = binding ?: return@observe
            binding.tvTitle.setText(title)
        }
        viewModel.description.observe(this) { description ->
            val binding = binding ?: return@observe
            binding.tvDescription.setText(description)
        }
        viewModel.action.observe(this) { state ->
            val binding = binding ?: return@observe
            binding.btnGrant.tvAction.setText(state.text)
            binding.btnGrant.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    private fun requestOverlayPermission() {
        if (PermissionManager.hasOverlayPermission()) {
            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
            return
        }
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = "package:${requireContext().packageName}".toUri()
        startForResult.launch(intent)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Chỉ post Cancel khi người dùng thực sự huỷ (không phải sau khi grant permission)
        if (!permissionGranted) {
            AppEventBus.post(AppEvent.PermissionCancel)
        }
    }

    companion object {
        const val TAG = "OverlayPermissionBottomSheet"
    }
}

@Deeplink
class OverlayPermissionDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PERMISSION_OVERLAY

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        OverlayPermissionBottomSheet().show(fragmentActivity.supportFragmentManager, OverlayPermissionBottomSheet.TAG)

        return true
    }
}
