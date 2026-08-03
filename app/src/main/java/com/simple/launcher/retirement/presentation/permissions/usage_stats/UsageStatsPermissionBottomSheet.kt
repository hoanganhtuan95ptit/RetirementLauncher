package com.simple.launcher.retirement.presentation.permissions.usage_stats

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
import androidx.core.view.updateLayoutParams
import com.simple.launcher.retirement.databinding.BottomSheetUsageStatsPermissionBinding
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

class UsageStatsPermissionBottomSheet : BaseBottomSheetDialogFragment<BottomSheetUsageStatsPermissionBinding, UsageStatsPermissionViewModel>() {

    override val viewModel: UsageStatsPermissionViewModel by viewModels()

    // Tránh double-post: khi permission được grant thì dismiss() → onDismiss không post Cancel nữa
    private var permissionGranted = false

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        if (PermissionManager.hasUsageStatsPermission()) {

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

        binding.cbUnderstand.visibility = View.VISIBLE
        binding.btnDecline.root.visibility = View.VISIBLE

        binding.cbUnderstand.setOnCheckedChangeListener { _, isChecked ->

            viewModel.isAgreed.value = isChecked
        }

        binding.btnGrant.root.setOnSafeClickListener {

            if (viewModel.isAgreed.value) {

                requestUsageStatsPermission()
            }
        }

        binding.btnDecline.root.setOnSafeClickListener {

            dismiss()
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
        viewModel.checkboxText.observe(this) { text ->

            val binding = binding ?: return@observe
            binding.cbUnderstand.setText(text)
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

        viewModel.checkboxMarginTop.observe(this) { margin ->

            val binding = binding ?: return@observe
            binding.cbUnderstand.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                topMargin = margin
            }
        }

        viewModel.grantButtonMarginTop.observe(this) { margin ->

            val binding = binding ?: return@observe
            binding.btnGrant.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                topMargin = margin
            }
        }

        viewModel.declineButtonMarginTop.observe(this) { margin ->

            val binding = binding ?: return@observe
            binding.btnDecline.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                topMargin = margin
            }
        }

        viewModel.action.observe(this) { state ->

            val binding = binding ?: return@observe
            binding.btnGrant.tvAction.setText(state.text)
            binding.btnGrant.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
            binding.btnGrant.root.isEnabled = state.isEnabled
        }

        viewModel.declineAction.observe(this) { state ->

            val binding = binding ?: return@observe
            binding.btnDecline.tvAction.setText(state.text)
            binding.btnDecline.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    private fun requestUsageStatsPermission() {

        startForResult.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    override fun onDismiss(dialog: DialogInterface) {

        super.onDismiss(dialog)
        // Chỉ post Cancel khi người dùng thực sự huỷ (không phải sau khi grant permission)
        if (!permissionGranted) {

            AppEventBus.post(AppEvent.PermissionCancel)
        }
    }

    companion object {

        const val TAG = "UsageStatsPermissionBottomSheet"
    }
}

@Deeplink
class UsageStatsPermissionDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.PERMISSION_USAGE_STATS

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        UsageStatsPermissionBottomSheet().show(fragmentActivity.supportFragmentManager, UsageStatsPermissionBottomSheet.TAG)

        return true
    }
}
