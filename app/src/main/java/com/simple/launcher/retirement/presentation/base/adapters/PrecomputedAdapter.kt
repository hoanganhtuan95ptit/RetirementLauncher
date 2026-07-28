package com.simple.launcher.retirement.presentation.base.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemNodeBinding
import com.simple.launcher.retirement.utils.exts.setOnSafeWithPerformHapticFeedbackClickListener
import com.simple.launcher.retirement.utils.exts.getItem
import com.simple.ui.precompute.DrawSpec
import com.simple.ui.precompute.PrecomputedView

abstract class PrecomputedViewItem : ViewItem {

    lateinit var spec: DrawSpec

    abstract fun buildDrawSpec(resources: Map<String, Any>)
}

abstract class PrecomputedAdapter<T : PrecomputedViewItem> : ViewItemAdapter<T, ItemNodeBinding>() {

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemNodeBinding {

        return ItemNodeBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemNodeBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)

        viewHolder.itemView.setOnSafeWithPerformHapticFeedbackClickListener {

            val item = viewHolder.getItem<PrecomputedViewItem>() ?: return@setOnSafeWithPerformHapticFeedbackClickListener

            if (!viewItemClass.isInstance(item)) {

                return@setOnSafeWithPerformHapticFeedbackClickListener
            }

            @Suppress("UNCHECKED_CAST")
            onItemCLick(viewHolder.binding.root, item as T)
        }

        return viewHolder
    }

    open fun onItemCLick(viewItem: PrecomputedView, item: T) {

        onItemCLick(item)
    }

    open fun onItemCLick(item: T) {

    }

    override fun onBindViewHolder(binding: ItemNodeBinding, viewType: Int, position: Int, item: T, payloads: List<String>) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)
        binding.nodeView.spec = item.spec
    }
}
