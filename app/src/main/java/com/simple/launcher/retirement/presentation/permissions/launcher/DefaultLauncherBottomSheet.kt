package com.simple.launcher.retirement.presentation.permissions.launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import androidx.core.view.updateLayoutParams
import com.simple.launcher.retirement.databinding.BottomSheetDefaultLauncherBinding
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

class DefaultLauncherBottomSheet : BaseBottomSheetDialogFragment<BottomSheetDefaultLauncherBinding, DefaultLauncherViewModel>() {

    override val viewModel: DefaultLauncherViewModel by viewModels()

    // Tránh double-post: khi permission được grant thì dismiss() → onDismiss không post Cancel nữa
    private var permissionGranted = false

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (PermissionManager.isDefaultLauncher()) {
            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetDefaultLauncherBinding {
        return BottomSheetDefaultLauncherBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.btnSetDefault.root.setOnSafeClickListener {
            openDefaultLauncherSettings()
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

        viewModel.horizontalPadding.observe(this) { padding ->

            val binding = binding ?: return@observe
            binding.root.setPadding(padding, binding.root.paddingTop, padding, binding.root.paddingBottom)
        }

        viewModel.topPadding.observe(this) { padding ->

            val binding = binding ?: return@observe
            binding.root.setPadding(binding.root.paddingLeft, padding, binding.root.paddingRight, binding.root.paddingBottom)
        }

        viewModel.descriptionMarginTop.observe(this) { margin ->

            val binding = binding ?: return@observe
            binding.tvDescription.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                topMargin = margin
            }
        }

        viewModel.grantButtonMarginTop.observe(this) { margin ->

            val binding = binding ?: return@observe
            binding.btnSetDefault.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                topMargin = margin
            }
        }

        viewModel.action.observe(this) { state ->
            val binding = binding ?: return@observe
            binding.btnSetDefault.tvAction.setText(state.text)
            binding.btnSetDefault.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    private fun openDefaultLauncherSettings() {
        if (PermissionManager.isDefaultLauncher()) {
            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                startForResult.launch(intent)
            } else {
                openHomeSettingsFallback()
            }
        } else {
            openHomeSettingsFallback()
        }
    }

    private fun openHomeSettingsFallback() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
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
        const val TAG = "DefaultLauncherBottomSheet"
    }
}

@Deeplink
class DefaultLauncherDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PERMISSION_DEFAULT_LAUNCHER

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        DefaultLauncherBottomSheet().show(fragmentActivity.supportFragmentManager, DefaultLauncherBottomSheet.TAG)

        return true
    }
}
