package com.simple.launcher.retirement.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemUtilityBinding
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.Bold
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.text.withFirst
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop

data class CleanMemoryHomeItem(val memoryMB: Long) : HomeItem {
    override fun areItemsTheSame(): List<Any> = listOf("CleanMemory")
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        memoryMB to "memoryMB"
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

        if (payloads.isEmpty() || payloads.contains("memoryMB")) {
            val memory = "(${item.memoryMB})"
            binding.tvLabel.setText("Boost $memory".withFirst(memory, Bold))
            binding.ivIcon.setImage(ImageRes(android.R.drawable.ic_lock_power_off), CenterInside(), CircleCrop())
        }
    }
}
