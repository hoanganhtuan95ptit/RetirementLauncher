package com.simple.launcher.retirement.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemNodeBinding
import com.simple.ui.precompute.DrawSpec

data class HeaderHomeItem(
    val title: String,
    val spec: DrawSpec
) : HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS

    override fun areItemsTheSame(): List<Any> = listOf(
        title
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        spec to "spec"
    )
}

@Adapter
class HeaderAdapter : ViewItemAdapter<HeaderHomeItem, ItemNodeBinding>() {

    override val viewItemClass: Class<HeaderHomeItem> by lazy {

        HeaderHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemNodeBinding {

        return ItemNodeBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemNodeBinding, viewType: Int, position: Int, item: HeaderHomeItem, payloads: List<String>) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("spec")) {

            binding.nodeView.spec = item.spec
        }
    }
}
