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
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
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

        setupToolbar(binding)
        setupRecyclerView(binding)
        setupSaveButton(binding)
    }

    private fun setupToolbar(binding: FragmentSosSettingsBinding) {

        binding.toolbar.ivLeft.setOnSafeClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView(binding: FragmentSosSettingsBinding) {

        binding.rvSettings.apply {

            layoutManager = GridLayoutManager(requireContext(), 2).apply {

                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

                    override fun getSpanSize(position: Int): Int {

                        return (viewModel.viewItemList.value.getOrNull(position) as? SpanSizeLookupViewItem)?.getSpanSize() ?: 2
                    }
                }
            }
        }
    }

    private fun setupSaveButton(binding: FragmentSosSettingsBinding) {

        binding.btnSave.root.setOnSafeClickListener {

            viewLifecycleOwner.lifecycleScope.launch {

                val config = viewModel.save()

                val result = AppEventBus.events
                    .onSubscription { AppEventBus.post(AppEvent.SOSUpdate(config)) }
                    .filter { it is AppEvent.SOSUpdateSuccess || it is AppEvent.SOSUpdateCancel }
                    .first()

                if (result is AppEvent.SOSUpdateSuccess) {

                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    override fun observeData() {

        super.observeData()

        observeToolbar()
        observeViewItemList()
        observeSaveAction()
        observeItemClicks()
        observeTimeoutSelection()
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

    private fun observeViewItemList() {

        viewModel.viewItemList.attachAdapter().observe(this@SOSSettingsFragment) { (items, adapters) ->

            val binding = binding ?: return@observe

            binding.rvSettings.submitListAndAwait(items, adapters, true)
        }
    }

    private fun observeSaveAction() {

        viewModel.saveAction.observe(this@SOSSettingsFragment) { state ->

            val binding = binding ?: return@observe

            binding.btnSave.tvAction.setText(state.text)
            binding.btnSave.ivAction.isVisible = state.imageShow
            binding.btnSave.ivAction.setImage(state.image)
            binding.btnSave.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    private fun observeItemClicks() {

        AppEventBus.events
            .filterIsInstance<AppEvent.SOSItemClicked>()
            .observe(this@SOSSettingsFragment) { event ->

                handleItemClick(event.id)
            }
    }

    private fun observeTimeoutSelection() {

        AppEventBus.events
            .filterIsInstance<AppEvent.SOSTimeoutSelected>()
            .observe(this@SOSSettingsFragment) { event ->

                viewModel.updateTimeout(event.timeoutMillis)
            }
    }

    private fun handleItemClick(id: Int) {

        when (id) {

            SettingItem.ID_EMERGENCY_CALL_TOGGLE -> viewModel.toggleFeatureDraft()
            SOSSettingsViewModel.ID_TIMEOUT -> showTimeoutDialog()
            SOSSettingsViewModel.ID_ADD_PERIOD -> showAddTimePeriod()
            else -> handlePeriodItemClick(id)
        }
    }

    private fun handlePeriodItemClick(id: Int) {

        if (id < SOSSettingsViewModel.ID_PERIOD_ITEM_BASE) {

            return
        }

        val index = id - SOSSettingsViewModel.ID_PERIOD_ITEM_BASE
        val periodId = viewModel.exclusionPeriods.value.getOrNull(index)?.id ?: return

        viewModel.removeExclusionPeriod(periodId)
    }

    private fun showTimeoutDialog() {

        val currentTimeoutMillis = viewModel.timeout.value

        SOSTimeoutBottomSheet(currentTimeoutMillis).show(
            parentFragmentManager,
            SOSTimeoutBottomSheet.TAG
        )
    }

    private fun showAddTimePeriod() {

        viewLifecycleOwner.lifecycleScope.launch {

            val startTime = pickTime(R.string.sos_select_start_time, 22, 0) ?: return@launch
            val endTime = pickTime(R.string.sos_select_end_time, 7, 0) ?: return@launch

            val period = ExclusionPeriod(
                id = UUID.randomUUID().toString(),
                startHour = startTime.first,
                startMinute = startTime.second,
                endHour = endTime.first,
                endMinute = endTime.second
            )

            viewModel.addExclusionPeriod(period)
        }
    }

    private suspend fun pickTime(titleRes: Int, hour: Int, minute: Int): Pair<Int, Int>? = suspendCancellableCoroutine { cont ->

        val dialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->

            cont.resume(selectedHour to selectedMinute)
        }, hour, minute, true).apply {

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
