package com.simple.launcher.retirement.presentation.emergency.permissions

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.text.setText

class EmergencyContactRequiredBottomSheet :
    BaseBottomSheetDialogFragment<BottomSheetUsageStatsPermissionBinding, EmergencyContactRequiredViewModel>() {

    override val viewModel: EmergencyContactRequiredViewModel by viewModels()

    private var hasUserAccepted = false

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetUsageStatsPermissionBinding {

        return BottomSheetUsageStatsPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.btnGrant.root.setOnSafeClickListener {

            hasUserAccepted = true
            AppEventBus.post(AppEvent.EmergencyContactRequiredAccept)
            dismiss()
        }
    }

    override fun observeData() {

        super.observeData()

        observeRequiredContactTitle()
        observeRequiredContactDescription()
        observeRequiredContactAction()
    }

    private fun observeRequiredContactTitle() {

        viewModel.title.observe(this) { title ->

            val binding = binding ?: return@observe
            binding.tvTitle.setText(title)
        }
    }

    private fun observeRequiredContactDescription() {

        viewModel.description.observe(this) { description ->

            val binding = binding ?: return@observe
            binding.tvDescription.setText(description)
        }
    }

    private fun observeRequiredContactAction() {

        viewModel.action.observe(this) { state ->

            val binding = binding ?: return@observe
            binding.btnGrant.tvAction.setText(state.text)
            binding.btnGrant.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {

        super.onDismiss(dialog)

        if (!hasUserAccepted) {

            // Người dùng đóng sheet mà chưa xác nhận thì PermissionManager cần nhận kết quả cancel.
            AppEventBus.post(AppEvent.EmergencyContactRequiredCancel)
        }
    }

    companion object {

        const val TAG = "EmergencyContactRequiredBottomSheet"
    }
}

@Deeplink
class EmergencyContactRequiredDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.EMERGENCY_CONTACT_REQUIRED

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {

        EmergencyContactRequiredBottomSheet().show(
            fragmentActivity.supportFragmentManager,
            EmergencyContactRequiredBottomSheet.TAG
        )
        return true
    }
}
