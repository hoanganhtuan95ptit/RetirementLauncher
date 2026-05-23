package com.simple.launcher.retirement.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ItemHeaderBinding
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.setText

data class SettingHeaderItem(
    val title: RichText
) : ViewItem, SpanSizeLookupViewItem {
    override fun getSpanSize(): Int = 2
    override fun areItemsTheSame(): List<Any> = listOf(title.text)
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(title to "title")
}

@Adapter
class SettingHeaderAdapter : ViewItemAdapter<SettingHeaderItem, ItemHeaderBinding>() {

    override val viewItemClass: Class<SettingHeaderItem> by lazy {
        SettingHeaderItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemHeaderBinding {
        return ItemHeaderBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemHeaderBinding, viewType: Int, position: Int, item: SettingHeaderItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        binding.tvTitle.setText(item.title)
        binding.tvTitle.setPadding(
            binding.root.context.resources.getDimensionPixelSize(R.dimen.setting_header_padding_horizontal),
            binding.root.context.resources.getDimensionPixelSize(R.dimen.setting_header_padding_top),
            binding.root.context.resources.getDimensionPixelSize(R.dimen.setting_header_padding_horizontal),
            binding.root.context.resources.getDimensionPixelSize(R.dimen.setting_header_padding_bottom)
        )
    }
}
