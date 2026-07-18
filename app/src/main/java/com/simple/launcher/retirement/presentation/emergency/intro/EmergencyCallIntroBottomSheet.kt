package com.simple.launcher.retirement.presentation.emergency.intro

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

class EmergencyCallIntroBottomSheet : BaseBottomSheetDialogFragment<BottomSheetUsageStatsPermissionBinding, EmergencyCallIntroViewModel>() {

    override val viewModel: EmergencyCallIntroViewModel by viewModels()

    private var isAccepted = false

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetUsageStatsPermissionBinding {
        return BottomSheetUsageStatsPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding?.btnGrant?.root?.setOnSafeClickListener {
            isAccepted = true
            AppEventBus.post(AppEvent.EmergencyCallIntroAccept)
            dismiss()
        }
    }

    override fun observeData() {
        super.observeData()

        viewModel.title.observe(this) { title ->
            binding?.tvTitle?.setText(title)
        }
        viewModel.description.observe(this) { description ->
            binding?.tvDescription?.setText(description)
        }
        viewModel.action.observe(this) { state ->
            binding?.btnGrant?.tvAction?.setText(state.text)
            binding?.btnGrant?.tvAction?.parent?.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!isAccepted) {
            AppEventBus.post(AppEvent.EmergencyCallIntroCancel)
        }
    }

    companion object {
        const val TAG = "EmergencyCallIntroBottomSheet"
    }
}

@Deeplink
class EmergencyCallIntroDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.EMERGENCY_CALL_INTRO

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        EmergencyCallIntroBottomSheet().show(fragmentActivity.supportFragmentManager, EmergencyCallIntroBottomSheet.TAG)

        return true
    }
}
