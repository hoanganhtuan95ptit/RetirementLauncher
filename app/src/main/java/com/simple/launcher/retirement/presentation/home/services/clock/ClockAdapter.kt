package com.simple.launcher.retirement.presentation.home.services.clock

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemNodeBinding
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.ui.precompute.DrawSpec

data class ClockHomeItem(
    val spec: DrawSpec
) : HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS // full width

    override fun areItemsTheSame(): List<Any> = listOf("Clock")

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        spec to "spec"
    )
}

@Adapter
class ClockAdapter : ViewItemAdapter<ClockHomeItem, ItemNodeBinding>() {

    override val viewItemClass: Class<ClockHomeItem> by lazy {
        ClockHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemNodeBinding {
        return ItemNodeBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemNodeBinding, viewType: Int, position: Int, item: ClockHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty() || payloads.contains("spec")) {
            binding.nodeView.spec = item.spec
        }
    }
}
