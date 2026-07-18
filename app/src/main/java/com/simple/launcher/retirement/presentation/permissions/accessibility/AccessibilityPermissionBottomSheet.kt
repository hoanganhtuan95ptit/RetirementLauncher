package com.simple.launcher.retirement.presentation.permissions.accessibility

import android.content.DialogInterface
import android.content.Intent
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
import com.simple.launcher.retirement.databinding.BottomSheetUsageStatsPermissionBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.text.setText

class AccessibilityPermissionBottomSheet : BaseBottomSheetDialogFragment<BottomSheetUsageStatsPermissionBinding, AccessibilityPermissionViewModel>() {

    override val viewModel: AccessibilityPermissionViewModel by viewModels()

    private var permissionGranted = false

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        if (PermissionManager.hasUserActivityAccessibilityPermission()) {

            permissionGranted = true
            AppEventBus.post(AppEvent.PermissionAccept)
            dismiss()
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetUsageStatsPermissionBinding {

        return BottomSheetUsageStatsPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return
        binding.btnGrant.root.setOnSafeClickListener {

            requestAccessibilityPermission()
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

    override fun onDismiss(dialog: DialogInterface) {

        super.onDismiss(dialog)
        if (!permissionGranted) {

            AppEventBus.post(AppEvent.PermissionCancel)
        }
    }

    private fun requestAccessibilityPermission() {

        startForResult.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    companion object {

        const val TAG = "AccessibilityPermissionBottomSheet"
    }
}

@Deeplink
class AccessibilityPermissionDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PERMISSION_USER_ACTIVITY_ACCESSIBILITY

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        AccessibilityPermissionBottomSheet().show(fragmentActivity.supportFragmentManager, AccessibilityPermissionBottomSheet.TAG)

        return true
    }
}
