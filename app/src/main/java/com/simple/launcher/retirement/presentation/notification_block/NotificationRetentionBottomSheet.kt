package com.simple.launcher.retirement.presentation.notification_block

import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.BottomSheetNotificationRetentionBinding
import com.simple.launcher.retirement.databinding.ItemSosTimeoutBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObject
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorOutline
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.ui.precompute.text.setText

/**
 * Bottom sheet chọn thời gian giữ notification trước khi tự xoá.
 * Bố cục / hành vi bám sát [com.simple.launcher.retirement.presentation.emergency.setting.SOSTimeoutBottomSheet]
 * để user quen với UI SOS thấy quen mắt.
 */
class NotificationRetentionBottomSheet(
    currentRetentionMillis: Long
) : BaseBottomSheetDialogFragment<BottomSheetNotificationRetentionBinding, NotificationRetentionViewModel>() {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    override val viewModel: NotificationRetentionViewModel by viewModels()

    private val selectedState = SelectedRetentionState(currentRetentionMillis)

    // ── 3. Public API ─────────────────────────────────────────────────────

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetNotificationRetentionBinding {

        return BottomSheetNotificationRetentionBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        setupRetentionOptionGrid(binding)
        setupConfirmAction(binding)
    }

    override fun observeData() {

        super.observeData()

        val binding = binding ?: return

        viewModel.title.observe(this) {

            binding.tvTitle.setText(it)
            binding.tvTitle.setPadding(0, 20.dp().toInt(), 0, 20.dp().toInt())
        }

        viewModel.confirmLabel.observe(this) {

            binding.btnChange.tvAction.setText(it)
            binding.btnChange.tvAction.parent.asObject<View>().setBackground(
                Background.Builder()
                    .backgroundColor(viewModel.resources.value.colorPrimary)
                    .cornerRadius(12.dp().toInt())
                    .stroke(1.dp().toInt(), viewModel.resources.value.colorOutline)
                    .build()
            )
        }

        viewModel.horizontalPadding.observe(this) { padding ->

            binding.rvRetention.setPadding(padding, 0, padding, 0)
            binding.btnChange.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                marginStart = padding
                marginEnd = padding
            }
        }

        viewModel.bottomPadding.observe(this) { padding ->

            binding.root.setPadding(0, 0, 0, padding)
        }

        viewModel.listMarginBottom.observe(this) { margin ->

            binding.rvRetention.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                bottomMargin = margin
            }
        }
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun setupRetentionOptionGrid(binding: BottomSheetNotificationRetentionBinding) {

        val resources = viewModel.resources.value
        val options = buildSelectableRetentionOptions()
        val adapter = RetentionAdapter(selectedState, resources)

        binding.rvRetention.apply {

            layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter = adapter
            addItemDecoration(GridSpacingItemDecoration(3, 8.dp().toInt()))
        }

        adapter.submitList(options)
    }

    private fun setupConfirmAction(binding: BottomSheetNotificationRetentionBinding) {

        binding.btnChange.root.setOnSafeClickListener {

            AppEventBus.post(AppEvent.NotificationRetentionSelected(selectedState.value))
            dismiss()
        }
    }

    private fun buildSelectableRetentionOptions(): List<RetentionOption> {

        // Off = 0: tắt tự xoá. Các mốc còn lại tính theo giờ.
        val offOption = RetentionOption(
            valueMillis = 0L,
            labelRes = R.string.notification_block_retention_off,
            labelValue = 0
        )

        val hourOptions = listOf(1, 2, 3, 6, 12, 24, 48, 72).map { hours ->

            RetentionOption(
                valueMillis = hours * HOUR_MILLIS,
                labelRes = R.string.notification_block_retention_hours,
                labelValue = hours
            )
        }

        return listOf(offOption) + hourOptions
    }

    // ── 5. Nested classes ─────────────────────────────────────────────────

    private class RetentionAdapter(
        private val selectedState: SelectedRetentionState,
        private val resources: Map<String, Any>
    ) : RecyclerView.Adapter<RetentionAdapter.ViewHolder>() {

        private var items = emptyList<RetentionOption>()

        fun submitList(newItems: List<RetentionOption>) {

            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(ItemSosTimeoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val item = items[position]
            val binding = holder.binding

            binding.tvTimeout.text = if (item.valueMillis == 0L) {

                binding.root.context.getString(item.labelRes)
            } else {

                binding.root.context.getString(item.labelRes, item.labelValue)
            }
            binding.tvTimeout.setPadding(16.dp().toInt(), 16.dp().toInt(), 16.dp().toInt(), 16.dp().toInt())
            binding.tvTimeout.minHeight = 56.dp().toInt()

            binding.tvTimeout.setOnClickListener {

                selectOption(item, position)
            }

            val isSelected = item.valueMillis == selectedState.value

            val bgColor = if (isSelected) resources.colorPrimary else android.graphics.Color.TRANSPARENT
            val textColor = if (isSelected) resources.colorOnPrimary else resources.textColorPrimary
            val strokeColor = resources.colorOutline

            binding.tvTimeout.setTextColor(textColor)
            binding.tvTimeout.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            binding.root.setBackground(
                Background.Builder()
                    .backgroundColor(bgColor)
                    .cornerRadius(12.dp().toInt())
                    .stroke(1.dp().toInt(), strokeColor)
                    .build()
            )
        }

        override fun getItemCount(): Int = items.size

        private fun selectOption(item: RetentionOption, position: Int) {

            val previous = selectedState.value
            selectedState.value = item.valueMillis
            refreshPrevious(previous)
            notifyItemChanged(position)
        }

        private fun refreshPrevious(previousMillis: Long) {

            val previousIndex = items.indexOfFirst { it.valueMillis == previousMillis }
            if (previousIndex >= 0) notifyItemChanged(previousIndex)
        }

        class ViewHolder(val binding: ItemSosTimeoutBinding) : RecyclerView.ViewHolder(binding.root)
    }

    private data class RetentionOption(
        val valueMillis: Long,
        val labelRes: Int,
        val labelValue: Int
    )

    private data class SelectedRetentionState(
        var value: Long
    )

    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {

            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) outRect.top = spacing
        }
    }

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        const val TAG = "NotificationRetentionBottomSheet"

        private const val HOUR_MILLIS = 60 * 60 * 1000L
    }
}
