package com.simple.launcher.retirement.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemHeaderBinding
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.setText

data class HeaderHomeItem(val title: RichText) : HomeItem {
    override fun areItemsTheSame(): List<Any> = listOf(title.text)
    override val spanSize: Int = HomeItem.TOTAL_COLUMNS // full width
}

@Adapter
class HeaderAdapter : ViewItemAdapter<HeaderHomeItem, ItemHeaderBinding>() {
    override val viewItemClass: Class<HeaderHomeItem> by lazy {
        HeaderHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemHeaderBinding {
        return ItemHeaderBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemHeaderBinding, viewType: Int, position: Int, item: HeaderHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        binding.tvTitle.setText(item.title)
    }
}
