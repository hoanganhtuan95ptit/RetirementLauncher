package com.simple.launcher.retirement.presentation.home.adapter

import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemUtilityBinding
import com.simple.launcher.retirement.utils.getItem
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

/**
 * Base adapter cho các utility item (CleanFiles, CleanMemory).
 * Gộp phần createViewHolder + click listener trùng lặp vào đây.
 */
abstract class UtilityAdapter<T : HomeItem> : ViewItemAdapter<T, ItemUtilityBinding>() {

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemUtilityBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeClickListener {
            @Suppress("UNCHECKED_CAST")
            val item = viewHolder.getItem<HomeItem>() as? T ?: return@setOnSafeClickListener
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            HomeEventBus.post(item)
        }
        return viewHolder
    }
}
