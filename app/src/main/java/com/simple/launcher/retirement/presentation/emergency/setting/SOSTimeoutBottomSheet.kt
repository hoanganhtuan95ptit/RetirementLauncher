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
import com.simple.launcher.retirement.databinding.BottomSheetSosTimeoutBinding
import com.simple.launcher.retirement.databinding.ItemSosTimeoutBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObject
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorOutline
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class SOSTimeoutBottomSheet(
    currentTimeoutMillis: Long
) : BaseBottomSheetDialogFragment<BottomSheetSosTimeoutBinding, BaseViewModel>() {

    override val viewModel: BaseViewModel by viewModels()

    private val timeoutSelection = TimeoutSelection(currentTimeoutMillis)

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetSosTimeoutBinding {

        return BottomSheetSosTimeoutBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        setupRecyclerView(binding)
        setupActionButtons(binding)
    }

    private fun setupRecyclerView(binding: BottomSheetSosTimeoutBinding) {

        val resources = viewModel.resources.value
        val timeoutOptions = buildTimeoutOptions()
        val adapter = TimeoutAdapter(timeoutSelection, resources)

        binding.rvTimeout.apply {

            layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter = adapter
            addItemDecoration(GridSpacingItemDecoration(3, 8.dp().toInt()))
        }

        adapter.submitList(timeoutOptions)
    }

    private fun setupActionButtons(binding: BottomSheetSosTimeoutBinding) {

        val resources = viewModel.resources.value

        binding.btnChange.tvAction.text = getString(R.string.sos_save_changes)
        binding.btnChange.tvAction.setTextColor(resources.colorOnPrimary)
        binding.btnChange.tvAction.parent.asObject<View>().setBackground(
            Background.Builder()
                .backgroundColor(resources.colorPrimary)
                .cornerRadius(12.dp().toInt())
                .stroke(1.dp().toInt(), resources.colorOutline)
                .build()
        )

        binding.btnChange.root.setOnSafeClickListener {

            AppEventBus.post(AppEvent.SOSTimeoutSelected(timeoutSelection.value))
            dismiss()
        }
    }

    private fun buildTimeoutOptions(): List<TimeoutOption> {

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
        private val timeoutSelection: TimeoutSelection,
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

            binding.tvTimeout.setOnClickListener {

                selectTimeout(item, position)
            }

            val isSelected = item.valueMillis == timeoutSelection.value

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

        private fun selectTimeout(item: TimeoutOption, position: Int) {

            val oldSelected = timeoutSelection.value
            timeoutSelection.value = item.valueMillis

            notifySelectedItemChanged(oldSelected)
            notifyItemChanged(position)
        }

        private fun notifySelectedItemChanged(selectedTimeoutMillis: Long) {

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

    private data class TimeoutSelection(
        var value: Long
    )

    private class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

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
