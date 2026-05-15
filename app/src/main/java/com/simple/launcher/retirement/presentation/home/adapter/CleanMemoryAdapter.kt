package com.simple.launcher.retirement.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemUtilityBinding
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.setText
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop

data class CleanMemoryHomeItem(val label: RichText, val icon: RichImage) : HomeItem {
    override fun areItemsTheSame(): List<Any> = listOf("CleanMemory")
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label"
    )
    override val spanSize: Int = HomeItem.TOTAL_COLUMNS / 2 // half width
}

@Adapter
class CleanMemoryAdapter : UtilityAdapter<CleanMemoryHomeItem>() {
    override val viewItemClass: Class<CleanMemoryHomeItem> by lazy {
        CleanMemoryHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemUtilityBinding {
        return ItemUtilityBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemUtilityBinding, viewType: Int, position: Int, item: CleanMemoryHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty()) {
            binding.ivIcon.setImage(item.icon, CenterInside(), CircleCrop())
        }
        if (payloads.isEmpty() || payloads.contains("label")) {
            binding.tvLabel.setText(item.label)
        }
    }
}
