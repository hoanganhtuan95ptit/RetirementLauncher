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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

data class CleanFilesHomeItem(val label: RichText, val icon: RichImage) : HomeItem {
    override fun areItemsTheSame(): List<Any> = listOf("CleanFiles")
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label"
    )
    override val spanSize: Int = HomeItem.TOTAL_COLUMNS / 2 // half width
}

@Adapter
class CleanFilesAdapter : UtilityAdapter<CleanFilesHomeItem>() {
    override val viewItemClass: Class<CleanFilesHomeItem> by lazy {
        CleanFilesHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemUtilityBinding {
        return ItemUtilityBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemUtilityBinding, viewType: Int, position: Int, item: CleanFilesHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty()) {
            binding.ivIcon.setImage(item.icon, CenterInside(), RoundedCorners(24))
        }
        if (payloads.isEmpty() || payloads.contains("label")) {
            binding.tvLabel.setText(item.label)
        }
    }
}
