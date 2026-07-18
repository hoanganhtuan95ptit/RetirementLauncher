package com.simple.launcher.retirement.presentation.emergency.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemSosSectionHeaderBinding
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class SOSSectionHeaderViewItem(
    val title: BigText,
    val icon: BigImage,
    val isEnabled: Boolean = true
) : ViewItem, SpanSizeLookupViewItem {

    override fun getSpanSize(): Int = 2

    override fun areItemsTheSame(): List<Any> = listOf(title)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",
        icon to "icon",
        isEnabled to "isEnabled"
    )
}

@Adapter
class SOSSectionHeaderAdapter : ViewItemAdapter<SOSSectionHeaderViewItem, ItemSosSectionHeaderBinding>() {

    override val viewItemClass: Class<SOSSectionHeaderViewItem> = SOSSectionHeaderViewItem::class.java

    override fun createViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSosSectionHeaderBinding {

        return ItemSosSectionHeaderBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(
        binding: ItemSosSectionHeaderBinding,
        viewType: Int,
        position: Int,
        item: SOSSectionHeaderViewItem,
        payloads: List<String>
    ) {

        val alpha = if (item.isEnabled) 1f else 0.5f

        if (payloads.isEmpty() || payloads.contains("isEnabled")) {

            binding.tvSectionTitle.alpha = alpha
            binding.ivSectionIcon.alpha = alpha
        }

        if (payloads.isEmpty() || payloads.contains("title")) {

            binding.tvSectionTitle.setText(item.title)
        }

        if (payloads.isEmpty() || payloads.contains("icon")) {

            binding.ivSectionIcon.setImage(item.icon)
        }
    }
}
