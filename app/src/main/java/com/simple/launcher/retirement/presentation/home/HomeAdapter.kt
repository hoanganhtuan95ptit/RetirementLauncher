package com.simple.launcher.retirement.presentation.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ItemAppBinding
import com.simple.launcher.retirement.databinding.ItemClockBinding
import com.simple.launcher.retirement.databinding.ItemUtilityBinding
import com.simple.launcher.retirement.domain.model.HomeItem

class HomeAdapter(
    private val items: List<HomeItem>,
    private val onItemClick: (HomeItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CLOCK = 0
        private const val TYPE_APP = 1
        private const val TYPE_UTILITY = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeItem.Clock -> TYPE_CLOCK
            is HomeItem.CleanFiles, is HomeItem.CleanMemory -> TYPE_UTILITY
            else -> TYPE_APP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CLOCK -> {
                val binding = ItemClockBinding.inflate(inflater, parent, false)
                ClockViewHolder(binding)
            }
            TYPE_UTILITY -> {
                val binding = ItemUtilityBinding.inflate(inflater, parent, false)
                UtilityViewHolder(binding)
            }
            else -> {
                val binding = ItemAppBinding.inflate(inflater, parent, false)
                AppViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ClockViewHolder -> {
                // TextClock handles itself
            }
            is UtilityViewHolder -> {
                val context = holder.binding.root.context
                with(holder.binding) {
                    ivIcon.setBackgroundResource(0)
                    ivIcon.setPadding(0, 0, 0, 0)

                    when (item) {
                        is HomeItem.CleanFiles -> {
                            tvLabel.text = context.getString(R.string.home_strange_files, item.fileCount)
                            ivIcon.setImageResource(android.R.drawable.ic_menu_delete)
                            ivIcon.setBackgroundResource(android.R.color.holo_orange_light)
                            ivIcon.setPadding(16, 16, 16, 16)
                        }
                        is HomeItem.CleanMemory -> {
                            tvLabel.text = context.getString(R.string.home_memory_status, item.memoryMB)
                            ivIcon.setImageResource(android.R.drawable.ic_media_play)
                            ivIcon.setBackgroundResource(android.R.color.holo_blue_light)
                            ivIcon.setPadding(16, 16, 16, 16)
                        }
                        else -> {}
                    }
                    root.setOnClickListener { onItemClick(item) }
                }
            }
            is AppViewHolder -> {
                with(holder.binding) {
                    ivIcon.setBackgroundResource(0)
                    ivIcon.setPadding(0, 0, 0, 0)

                    when (item) {
                        is HomeItem.App -> {
                            tvLabel.text = item.entity.label
                            ivIcon.setImageDrawable(item.entity.icon)
                        }
                        is HomeItem.Contact -> {
                            tvLabel.text = item.entity.name
                            if (item.entity.photoUri != null) {
                                ivIcon.setImageURI(Uri.parse(item.entity.photoUri))
                            } else {
                                ivIcon.setImageResource(android.R.drawable.sym_action_call)
                                ivIcon.setBackgroundResource(android.R.color.holo_green_light)
                                ivIcon.setPadding(16, 16, 16, 16)
                            }
                        }
                        else -> {}
                    }
                    root.setOnClickListener { onItemClick(item) }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    class AppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)
    class UtilityViewHolder(val binding: ItemUtilityBinding) : RecyclerView.ViewHolder(binding.root)
    class ClockViewHolder(val binding: ItemClockBinding) : RecyclerView.ViewHolder(binding.root)
}
