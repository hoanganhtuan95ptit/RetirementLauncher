package com.simple.launcher.retirement.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemUtilityBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.view.setOnSafeWithPerformHapticFeedbackClickListener
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class CleanMemoryHomeItem(
    val label: BigText,
    val value: BigText,
    val icon: BigImage,
    val background: Background
) : HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS / 2 // half width

    override fun areItemsTheSame(): List<Any> = listOf(
        "CleanMemory"
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label",
        value to "value",
        icon to "icon",
        background to "background"
    )
}

@Adapter
class CleanMemoryAdapter : UtilityAdapter<CleanMemoryHomeItem>() {

    override val viewItemClass: Class<CleanMemoryHomeItem> by lazy {
        CleanMemoryHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemUtilityBinding {
        return ItemUtilityBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemUtilityBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeWithPerformHapticFeedbackClickListener {
            sendDeeplinkWithBackStack(DeepLinks.CLEAN_MEMORY)
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemUtilityBinding, viewType: Int, position: Int, item: CleanMemoryHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty() || payloads.contains("icon")) {
            binding.ivIcon.setImage(item.icon)
        }
        if (payloads.isEmpty() || payloads.contains("label")) {
            binding.tvLabel.setText(item.label)
        }
        if (payloads.isEmpty() || payloads.contains("value")) {
            binding.tvValue.setText(item.value)
        }
        if (payloads.isEmpty() || payloads.contains("background")) {
            binding.root.setBackground(item.background)
        }
    }
}
