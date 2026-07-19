package com.simple.launcher.retirement.presentation.reorder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemReorderBinding
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText

@Adapter
class ReorderAdapter : ViewItemAdapter<ReorderItem, ItemReorderBinding>() {

    override val viewItemClass: Class<ReorderItem> by lazy {
        ReorderItem::class.java
    }

    override fun createViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemReorderBinding {

        return ItemReorderBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(
        binding: ItemReorderBinding,
        viewType: Int,
        position: Int,
        item: ReorderItem,
        payloads: List<String>
    ) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)
        with(binding) {

            tvLabel.setText(item.label)
            ivIcon.setImage(item.icon)
        }
    }
}
