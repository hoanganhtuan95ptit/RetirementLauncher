package com.simple.launcher.retirement.presentation.settings.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemSettingHeaderBinding
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.setPadding
import com.simple.launcher.retirement.utils.size.Padding
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class SettingHeaderItem(
    val title: BigText,
    val padding: Padding = Padding(16.dp(), 24.dp(), 16.dp(), 8.dp())
) : ViewItem, SpanSizeLookupViewItem {

    override fun getSpanSize(): Int = 2

    override fun areItemsTheSame(): List<Any> = listOf(title.text)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",
        padding to "padding"
    )
}

@Adapter
class SettingHeaderAdapter : ViewItemAdapter<SettingHeaderItem, ItemSettingHeaderBinding>() {

    override val viewItemClass: Class<SettingHeaderItem> by lazy {

        SettingHeaderItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemSettingHeaderBinding {

        return ItemSettingHeaderBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemSettingHeaderBinding, viewType: Int, position: Int, item: SettingHeaderItem, payloads: List<String>) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty() || payloads.contains("title")) {

            binding.tvTitle.setText(item.title)
        }
        if (payloads.isEmpty() || payloads.contains("padding")) {

            binding.tvTitle.setPadding(item.padding)
        }
    }
}
