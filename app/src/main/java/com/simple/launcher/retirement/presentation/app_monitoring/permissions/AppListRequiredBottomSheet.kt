package com.simple.launcher.retirement.presentation.app_monitoring

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
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.ui.precompute.text.setText

class AppListRequiredBottomSheet :
    BaseBottomSheetDialogFragment<BottomSheetUsageStatsPermissionBinding, AppListRequiredViewModel>() {

    override val viewModel: AppListRequiredViewModel by viewModels()

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
            AppEventBus.post(AppEvent.AppListRequiredAccept)
            dismiss()
        }
    }

    override fun observeData() {

        super.observeData()

        observeRequiredAppListTitle()
        observeRequiredAppListDescription()
        observeRequiredAppListAction()
    }

    private fun observeRequiredAppListTitle() {

        viewModel.title.observe(this) { title ->

            val binding = binding ?: return@observe
            binding.tvTitle.setText(title)
        }
    }

    private fun observeRequiredAppListDescription() {

        viewModel.description.observe(this) { description ->

            val binding = binding ?: return@observe
            binding.tvDescription.setText(description)
        }
    }

    private fun observeRequiredAppListAction() {

        viewModel.action.observe(this) { state ->

            val binding = binding ?: return@observe
            binding.btnGrant.tvAction.setText(state.text)
            binding.btnGrant.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {

        super.onDismiss(dialog)

        if (!hasUserAccepted) {

            AppEventBus.post(AppEvent.AppListRequiredCancel)
        }
    }

    companion object {

        const val TAG = "AppListRequiredBottomSheet"
    }
}

@Deeplink
class AppListRequiredDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.APP_LIST_REQUIRED

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {

        AppListRequiredBottomSheet().show(
            fragmentActivity.supportFragmentManager,
            AppListRequiredBottomSheet.TAG
        )
        return true
    }
}
