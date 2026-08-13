package com.simple.launcher.retirement.presentation.notification_block

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentNotificationBlockBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull

class NotificationBlockSettingsFragment : BaseFragment<FragmentNotificationBlockBinding>() {

    private val viewModel: NotificationBlockSettingsViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNotificationBlockBinding {

        return FragmentNotificationBlockBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.toolbar.ivLeft.setOnSafeClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.rvItems.layoutManager = createGridLayoutManager()

        binding.btnSave.root.setOnSafeClickListener {

            viewModel.save()
        }
    }

    override fun observeData() {

        super.observeData()

        viewModel.background.filterNotNull().observe(this) { background ->

            binding?.root?.setBackground(background)
        }

        viewModel.toolbar.observe(this) { state -> renderToolbar(state) }

        viewModel.viewItemList.attachAdapter().observe(this) { (items, adapters) ->

            binding?.rvItems?.submitListAndAwait(items, adapters, true)
        }

        viewModel.saveAction.observe(this) { state ->

            val binding = binding ?: return@observe
            binding.btnSave.tvAction.setText(state.text)
            binding.btnSave.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
            binding.btnSave.root.isEnabled = state.isEnabled
        }

        viewModel.saveResultFlow.observe(this) { success ->

            if (success) parentFragmentManager.popBackStack()
        }

        AppEventBus.events.filterIsInstance<AppEvent.NotificationBlockHeaderClicked>().observe(this) {

            viewModel.toggleFeatureDraft()
        }

        AppEventBus.events.filterIsInstance<AppEvent.NotificationBlockAppToggled>().observe(this) { event ->

            if (viewModel.isFeatureEnabledDraft.value) viewModel.toggleApp(event.entity)
        }

        AppEventBus.events.filterIsInstance<AppEvent.NotificationRetentionSelected>().observe(this) { event ->

            viewModel.updateRetentionDraft(event.retentionMillis)
        }

        // Card retention tái dùng SOSCardViewItem/Adapter — click sinh ra SOSItemClicked.
        // Chỉ mở picker khi id trùng với card retention của màn này để không đụng SOS.
        AppEventBus.events.filterIsInstance<AppEvent.SOSItemClicked>().observe(this) { event ->

            if (event.id == NotificationBlockSettingsViewModel.ID_RETENTION_CARD &&
                viewModel.isFeatureEnabledDraft.value) showRetentionPicker()
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun createGridLayoutManager(): GridLayoutManager {

        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {

                val item = viewModel.viewItemList.value.getOrNull(position)
                return (item as? SpanSizeLookupViewItem)?.getSpanSize() ?: 2
            }
        }
        return layoutManager
    }

    private fun renderToolbar(state: ToolbarState) {

        val binding = binding ?: return
        binding.toolbar.tvTitle.setText(state.title)
        val backIcon = state.backIcon
        binding.toolbar.ivLeft.isVisible = backIcon != null
        if (backIcon != null) binding.toolbar.ivLeft.setImage(backIcon)
    }

    private fun showRetentionPicker() {

        NotificationRetentionBottomSheet(viewModel.retentionMillisDraft.value).show(
            parentFragmentManager,
            NotificationRetentionBottomSheet.TAG
        )
    }
}

@Deeplink
class NotificationBlockSettingsDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.NOTIFICATION_BLOCK

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {

        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NotificationBlockSettingsFragment())

        if (extras?.get(DeepLinks.Extras.ADD_TO_BACK_STACK) == true) {

            transaction.addToBackStack(null)
        }

        transaction.commitAllowingStateLoss()
        return true
    }
}
