package com.simple.launcher.retirement.presentation.home.adapter

import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemUtilityBinding

/**
 * Base adapter cho các utility item (CleanFiles, CleanMemory).
 * Gộp phần createViewHolder + click listener trùng lặp vào đây.
 */
abstract class UtilityAdapter<T : HomeItem> : ViewItemAdapter<T, ItemUtilityBinding>() {

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemUtilityBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnClickListener {
            @Suppress("UNCHECKED_CAST")
            val item = (viewHolder.bindingAdapter as? ListAdapter<*, *>)
                ?.currentList
                ?.getOrNull(viewHolder.absoluteAdapterPosition) as? T
                ?: return@setOnClickListener
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            HomeEventBus.post(item)
        }
        return viewHolder
    }
}
