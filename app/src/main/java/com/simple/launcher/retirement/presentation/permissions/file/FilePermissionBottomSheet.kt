package com.simple.launcher.retirement.presentation.permissions.file

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.databinding.BottomSheetFilePermissionBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class FilePermissionBottomSheet : BaseBottomSheetDialogFragment<BottomSheetFilePermissionBinding, FilePermissionViewModel>() {

    override val viewModel: FilePermissionViewModel by viewModels()

    // Tránh double-post: khi permission được grant thì dismiss() → onDismiss không post Cancel nữa
    private var permissionGranted = false

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (PermissionManager.hasFilePermission()) {
            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        if (results.all { it.value }) {
            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetFilePermissionBinding {
        return BottomSheetFilePermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.btnGrant.root.setOnSafeClickListener {
            requestFilePermission()
        }
    }

    override fun observeData() {
        super.observeData()

        viewModel.title.observe(this) {
            binding.tvTitle.setText(it)
        }

        viewModel.message.observe(this) {
            binding.tvMessage.setText(it)
        }

        viewModel.action.observe(this) { state ->
            binding.btnGrant.tvAction.setText(state.text)
            binding.btnGrant.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    private fun requestFilePermission() {
        if (PermissionManager.hasFilePermission()) {
            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startForResult.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startForResult.launch(intent)
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Chỉ post Cancel khi người dùng thực sự huỷ (không phải sau khi grant permission)
        if (!permissionGranted) {
            AppEventBus.post(AppEvent.PermissionCancel)
        }
    }

    companion object {
        const val TAG = "FilePermissionBottomSheet"
    }
}

@Deeplink
class FilePermissionDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PERMISSION_FILE

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        FilePermissionBottomSheet().show(fragmentActivity.supportFragmentManager, FilePermissionBottomSheet.TAG)

        return true
    }
}
