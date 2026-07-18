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
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.BottomSheetSosTimeoutBinding
import com.simple.launcher.retirement.databinding.ItemSosTimeoutBinding
import com.simple.launcher.retirement.presentation.base.BaseBottomSheetDialogFragment
import com.simple.launcher.retirement.presentation.base.BaseViewModel
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
    currentTimeoutHours: Int,
    private val onTimeoutSelected: (Int) -> Unit
) : BaseBottomSheetDialogFragment<BottomSheetSosTimeoutBinding, BaseViewModel>() {

    override val viewModel: BaseViewModel by viewModels()

    private var selectedHour: Int = currentTimeoutHours

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
        val hours = (1..12).toList()
        val adapter = TimeoutAdapter(selectedHour, resources) { hour ->

            selectedHour = hour
        }

        binding.rvTimeout.apply {

            layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter = adapter
            addItemDecoration(GridSpacingItemDecoration(3, 8.dp().toInt()))
        }

        adapter.submitList(hours)
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

            onTimeoutSelected(selectedHour)
            dismiss()
        }
    }

    private class TimeoutAdapter(
        private var selectedHour: Int,
        private val resources: Map<String, Any>,
        private val onItemSelected: (Int) -> Unit
    ) : RecyclerView.Adapter<TimeoutAdapter.ViewHolder>() {

        private var items = emptyList<Int>()

        fun submitList(newItems: List<Int>) {

            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(ItemSosTimeoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val item = items[position]
            val binding = holder.binding

            binding.tvTimeout.text = binding.root.context.getString(R.string.sos_timeout_value, item)

            binding.tvTimeout.setOnClickListener {

                val oldSelected = selectedHour
                selectedHour = item
                onItemSelected(item)

                notifyItemChanged(items.indexOf(oldSelected))
                notifyItemChanged(position)
            }

            val isSelected = item == selectedHour

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

        class ViewHolder(val binding: ItemSosTimeoutBinding) : RecyclerView.ViewHolder(binding.root)
    }

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
}
