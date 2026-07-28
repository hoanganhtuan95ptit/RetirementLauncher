package com.simple.launcher.retirement.presentation.permissions.call_block

import android.app.role.RoleManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.databinding.BottomSheetCallPermissionBinding
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

class CallBlockPermissionBottomSheet : BaseBottomSheetDialogFragment<BottomSheetCallPermissionBinding, CallBlockPermissionViewModel>() {

    override val viewModel: CallBlockPermissionViewModel by viewModels()

    // Tránh double-post: khi permission được grant thì dismiss() → onDismiss không post Cancel nữa
    private var permissionGranted = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->

        logDebug("permission results=$results")
        handlePermissionResults(results)
    }

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        logDebug("role request resultCode=${result.resultCode} | hasCallBlockPermissions=${PermissionManager.hasCallBlockPermissions()}")
        if (PermissionManager.hasCallBlockPermissions()) {
            onPermissionAccepted()
        }
    }

    // Fallback khi quyền bị từ chối vĩnh viễn: đưa user vào App Settings, quay lại thì check tiếp
    private val appSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        logDebug("back from app settings | hasRuntimePermissions=${hasRuntimePermissions()}")
        if (PermissionManager.hasCallBlockPermissions()) {
            onPermissionAccepted()
        } else if (hasRuntimePermissions()) {
            requestCallScreeningRole()
        }
    }

    private fun handlePermissionResults(results: Map<String, Boolean>) {

        if (results.all { it.value }) {

            requestCallScreeningRole()
            return
        }

        val deniedPermanently = results
            .filterValues { granted -> !granted }
            .keys
            .none { permission -> shouldShowRequestPermissionRationale(permission) }

        // Hệ thống sẽ không hiện dialog nữa nếu đã bị từ chối vĩnh viễn → phải mở App Settings
        if (deniedPermanently) {

            logDebug("permissions denied permanently → open app settings")
            openAppSettings()
        }
    }

    private fun hasRuntimePermissions(): Boolean {

        val context = context ?: return false
        return PermissionManager.getCallBlockPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestCallScreeningRole() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            onPermissionAccepted()
            return
        }

        val roleManager = context?.getSystemService(Context.ROLE_SERVICE) as? RoleManager ?: return

        if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {

            onPermissionAccepted()
            return
        }

        if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {

            Log.w(TAG, "ROLE_CALL_SCREENING không khả dụng trên thiết bị này")
            return
        }

        logDebug("launching role request ROLE_CALL_SCREENING")
        roleRequestLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
    }

    private fun openAppSettings() {

        val context = context ?: return
        val uri = Uri.fromParts("package", context.packageName, null)
        appSettingsLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
    }

    private fun logDebug(message: String) {

        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    private fun onPermissionAccepted() {

        permissionGranted = true
        AppEventBus.post(AppEvent.PermissionAccept)
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

        val binding = binding ?: return

        binding.btnGrant.root.setOnSafeClickListener {

            val allGranted = hasRuntimePermissions()
            logDebug("btnGrant clicked | hasRuntimePermissions=$allGranted")

            if (allGranted) {
                requestCallScreeningRole()
            } else {
                requestPermissionLauncher.launch(PermissionManager.getCallBlockPermissions())
            }
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
        // Chỉ post Cancel khi người dùng thực sự huỷ (không phải sau khi grant permission)
        if (!permissionGranted) {
            AppEventBus.post(AppEvent.PermissionCancel)
        }
    }

    companion object {
        const val TAG = "CallBlockPermissionBottomSheet"
    }
}

@Deeplink
class CallBlockPermissionDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PERMISSION_CALL_BLOCK

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        CallBlockPermissionBottomSheet().show(fragmentActivity.supportFragmentManager, CallBlockPermissionBottomSheet.TAG)

        return true
    }
}
