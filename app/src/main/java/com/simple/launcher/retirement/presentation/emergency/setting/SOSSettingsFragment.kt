package com.simple.launcher.retirement.presentation.emergency.setting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentSosSettingsBinding
import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

class SOSSettingsFragment : BaseFragment<FragmentSosSettingsBinding>() {

    private val viewModel: SOSSettingsViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSosSettingsBinding {

        return FragmentSosSettingsBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        setupBackNavigation(binding)
        setupSettingsGrid(binding)
        setupSaveConfigButton(binding)
    }

    private fun setupBackNavigation(binding: FragmentSosSettingsBinding) {

        binding.toolbar.ivLeft.setOnSafeClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSettingsGrid(binding: FragmentSosSettingsBinding) {

        binding.rvSettings.apply {

            layoutManager = GridLayoutManager(requireContext(), 2).apply {

                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

                    override fun getSpanSize(position: Int): Int {

                        val viewItem = viewModel.viewItemList.value.getOrNull(position)

                        return (viewItem as? SpanSizeLookupViewItem)?.getSpanSize() ?: 2
                    }
                }
            }
        }
    }

    private fun setupSaveConfigButton(binding: FragmentSosSettingsBinding) {

        binding.btnSave.root.setOnSafeClickListener {

            viewModel.saveEmergencyConfig()
        }
    }

    override fun observeData() {

        super.observeData()

        observeToolbar()
        observeSettingItems()
        observeSaveButtonState()
        observeSaveResult()
        observeSosItemClickEvents()
        observeTimeoutSelectedEvents()
    }

    private fun observeToolbar() {

        viewModel.toolbar.observe(this@SOSSettingsFragment) { state ->

            val binding = binding ?: return@observe

            binding.toolbar.tvTitle.setText(state.title)

            val backIcon = state.backIcon

            if (backIcon != null) {

                binding.toolbar.ivLeft.isVisible = true
                binding.toolbar.ivLeft.setImage(backIcon)
            } else {

                binding.toolbar.ivLeft.isVisible = false
            }
        }
    }

    private fun observeSettingItems() {

        viewModel.viewItemList.attachAdapter().observe(this@SOSSettingsFragment) { (items, adapters) ->

            val binding = binding ?: return@observe

            binding.rvSettings.submitListAndAwait(items, adapters, true)
        }
    }

    private fun observeSaveButtonState() {

        viewModel.saveAction.observe(this@SOSSettingsFragment) { state ->

            val binding = binding ?: return@observe

            binding.btnSave.tvAction.setText(state.text)
            binding.btnSave.ivAction.isVisible = state.imageShow
            binding.btnSave.ivAction.setImage(state.image)
            binding.btnSave.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    private fun observeSaveResult() {

        viewModel.saveResultFlow.observe(this@SOSSettingsFragment) { isSuccess ->

            if (isSuccess) {

                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun observeSosItemClickEvents() {

        AppEventBus.events
            .filterIsInstance<AppEvent.SOSItemClicked>()
            .observe(this@SOSSettingsFragment) { event ->

                handleSosSettingItemClick(event.id)
            }
    }

    private fun observeTimeoutSelectedEvents() {

        AppEventBus.events
            .filterIsInstance<AppEvent.SOSTimeoutSelected>()
            .observe(this@SOSSettingsFragment) { event ->

                viewModel.updateTimeoutDraft(event.timeoutMillis)
            }
    }

    private fun handleSosSettingItemClick(id: Int) {

        when (id) {

            SettingItem.ID_EMERGENCY_CALL_TOGGLE -> viewModel.toggleEmergencyFeatureDraft()
            SOSSettingsViewModel.ID_TIMEOUT -> showTimeoutSelectionSheet()
            SOSSettingsViewModel.ID_ADD_PERIOD -> showAddExclusionPeriodPicker()
            else -> removeExclusionPeriodByItemId(id)
        }
    }

    private fun removeExclusionPeriodByItemId(id: Int) {

        if (id < SOSSettingsViewModel.ID_PERIOD_ITEM_BASE) {

            return
        }

        val index = id - SOSSettingsViewModel.ID_PERIOD_ITEM_BASE
        val periodId = viewModel.exclusionPeriods.value.getOrNull(index)?.id ?: return

        viewModel.removeExclusionPeriodDraft(periodId)
    }

    private fun showTimeoutSelectionSheet() {

        val currentTimeoutMillis = viewModel.timeout.value

        SOSTimeoutBottomSheet(currentTimeoutMillis).show(
            parentFragmentManager,
            SOSTimeoutBottomSheet.TAG
        )
    }

    private fun showAddExclusionPeriodPicker() {

        viewLifecycleOwner.lifecycleScope.launch {

            // Chọn hai mốc liên tiếp để tạo khung giờ không tính vào timeout SOS.
            val startTime = pickClockTime(R.string.sos_select_start_time, 22, 0) ?: return@launch
            val endTime = pickClockTime(R.string.sos_select_end_time, 7, 0) ?: return@launch

            val period = ExclusionPeriod(
                id = UUID.randomUUID().toString(),
                startHour = startTime.first,
                startMinute = startTime.second,
                endHour = endTime.first,
                endMinute = endTime.second
            )

            viewModel.addExclusionPeriodDraft(period)
        }
    }

    private suspend fun pickClockTime(
        titleRes: Int,
        hour: Int,
        minute: Int
    ): Pair<Int, Int>? = suspendCancellableCoroutine { cont ->

        val dialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->

                cont.resume(selectedHour to selectedMinute)
            },
            hour,
            minute,
            true
        ).apply {

            setTitle(titleRes)
            setOnCancelListener { cont.resume(null) }
        }

        cont.invokeOnCancellation { dialog.dismiss() }
        dialog.show()
    }
}

@Deeplink
class SOSSettingsDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.SOS_SETTINGS

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {

        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SOSSettingsFragment())

        if (extras?.get(DeepLinks.Extras.ADD_TO_BACK_STACK) == true) {

            transaction.addToBackStack(null)
        }

        transaction.commitAllowingStateLoss()
        return true
    }
}
