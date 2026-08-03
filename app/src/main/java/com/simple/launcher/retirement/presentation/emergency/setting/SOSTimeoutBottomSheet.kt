package com.simple.launcher.retirement.presentation.emergency.setting

import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.R
import androidx.core.view.updateLayoutParams
import com.simple.launcher.retirement.databinding.BottomSheetSosTimeoutBinding
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

class SOSTimeoutBottomSheet(
    currentTimeoutMillis: Long
) : BaseBottomSheetDialogFragment<BottomSheetSosTimeoutBinding, SOSTimeoutViewModel>() {

    override val viewModel: SOSTimeoutViewModel by viewModels()

    private val selectedTimeoutState = SelectedTimeoutState(currentTimeoutMillis)

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetSosTimeoutBinding {

        return BottomSheetSosTimeoutBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        setupTimeoutOptionGrid(binding)
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

            binding.rvTimeout.setPadding(padding, 0, padding, 0)
            binding.btnChange.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                marginStart = padding
                marginEnd = padding
            }
        }

        viewModel.bottomPadding.observe(this) { padding ->

            binding.root.setPadding(0, 0, 0, padding)
        }

        viewModel.listMarginBottom.observe(this) { margin ->

            binding.rvTimeout.updateLayoutParams<ViewGroup.MarginLayoutParams> {

                bottomMargin = margin
            }
        }
    }

    private fun setupTimeoutOptionGrid(binding: BottomSheetSosTimeoutBinding) {

        val resources = viewModel.resources.value
        val timeoutOptions = buildSelectableTimeoutOptions()
        val adapter = TimeoutAdapter(selectedTimeoutState, resources)

        binding.rvTimeout.apply {

            layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter = adapter
            addItemDecoration(GridSpacingItemDecoration(3, 8.dp().toInt()))
        }

        adapter.submitList(timeoutOptions)
    }

    private fun setupConfirmAction(binding: BottomSheetSosTimeoutBinding) {

        binding.btnChange.root.setOnSafeClickListener {

            AppEventBus.post(AppEvent.SOSTimeoutSelected(selectedTimeoutState.value))
            dismiss()
        }
    }

    private fun buildSelectableTimeoutOptions(): List<TimeoutOption> {

        // DEBUG có thêm mốc vài giây để test SOS nhanh, production chỉ dùng mốc theo giờ.
        val debugOptions = if (BuildConfig.DEBUG) {

            listOf(5, 10, 15).map { seconds ->

                TimeoutOption(
                    valueMillis = seconds * SECOND_MILLIS,
                    labelRes = R.string.sos_timeout_seconds,
                    labelValue = seconds
                )
            }
        } else {

            emptyList()
        }

        val hourOptions = (1..12).map { hours ->

            TimeoutOption(
                valueMillis = hours * HOUR_MILLIS,
                labelRes = R.string.sos_timeout_value,
                labelValue = hours
            )
        }

        return debugOptions + hourOptions
    }

    private class TimeoutAdapter(
        private val selectedTimeoutState: SelectedTimeoutState,
        private val resources: Map<String, Any>
    ) : RecyclerView.Adapter<TimeoutAdapter.ViewHolder>() {

        private var items = emptyList<TimeoutOption>()

        fun submitList(newItems: List<TimeoutOption>) {

            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(ItemSosTimeoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val item = items[position]
            val binding = holder.binding

            binding.tvTimeout.text = binding.root.context.getString(item.labelRes, item.labelValue)
            binding.tvTimeout.setPadding(16.dp().toInt(), 16.dp().toInt(), 16.dp().toInt(), 16.dp().toInt())
            binding.tvTimeout.minHeight = 56.dp().toInt()

            binding.tvTimeout.setOnClickListener {

                selectTimeoutOption(item, position)
            }

            val isSelected = item.valueMillis == selectedTimeoutState.value

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

        private fun selectTimeoutOption(item: TimeoutOption, position: Int) {

            val previousTimeout = selectedTimeoutState.value
            selectedTimeoutState.value = item.valueMillis

            refreshPreviouslySelectedOption(previousTimeout)
            notifyItemChanged(position)
        }

        private fun refreshPreviouslySelectedOption(selectedTimeoutMillis: Long) {

            val selectedIndex = items.indexOfFirst { it.valueMillis == selectedTimeoutMillis }
            if (selectedIndex >= 0) {

                notifyItemChanged(selectedIndex)
            }
        }

        class ViewHolder(val binding: ItemSosTimeoutBinding) : RecyclerView.ViewHolder(binding.root)
    }

    private data class TimeoutOption(
        val valueMillis: Long,
        val labelRes: Int,
        val labelValue: Int
    )

    private data class SelectedTimeoutState(
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
            if (position >= spanCount) {

                outRect.top = spacing
            }
        }
    }

    companion object {

        const val TAG = "SOSTimeoutBottomSheet"

        private const val SECOND_MILLIS = 1000L
        private const val HOUR_MILLIS = 60 * 60 * 1000L
    }
}
