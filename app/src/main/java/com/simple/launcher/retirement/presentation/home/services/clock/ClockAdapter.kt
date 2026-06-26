package com.simple.launcher.retirement.presentation.home.services.clock

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemClockBinding
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground

data class ClockHomeItem(
    val background: Background
) : HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS // full width

    override fun areItemsTheSame(): List<Any> = listOf("Clock")

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        background to "background"
    )
}

@Adapter
class ClockAdapter : ViewItemAdapter<ClockHomeItem, ItemClockBinding>() {

    override val viewItemClass: Class<ClockHomeItem> by lazy {
        ClockHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemClockBinding {
        return ItemClockBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemClockBinding, viewType: Int, position: Int, item: ClockHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty() || payloads.contains("background")) {
            binding.root.setBackground(item.background)
        }
    }
}
