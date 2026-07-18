package com.simple.launcher.retirement.presentation.emergency.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSosHeaderBinding
import com.simple.launcher.retirement.presentation.settings.adapters.SettingItem
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class SOSHeaderViewItem(
    val title: BigText,
    val desc: BigText,
    val icon: BigImage,
    val iconBackground: Background = Background(),
    val isEnabled: Boolean
) : ViewItem, SpanSizeLookupViewItem {

    override fun getSpanSize(): Int = 2

    override fun areItemsTheSame(): List<Any> = listOf(
        "SOSHeader"
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",
        desc to "desc",
        icon to "icon",
        iconBackground to "iconBackground",
        isEnabled to "isEnabled"
    )
}

@Adapter
class SOSHeaderAdapter : ViewItemAdapter<SOSHeaderViewItem, ItemSosHeaderBinding>() {

    override val viewItemClass: Class<SOSHeaderViewItem> = SOSHeaderViewItem::class.java

    override fun createViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSosHeaderBinding {

        return ItemSosHeaderBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemSosHeaderBinding> {

        val viewHolder = super.createViewHolder(parent, viewType)

        viewHolder.binding.vClickToggle.setOnSafeClickListener {

            val viewItem = (viewHolder.getViewItem() as? SOSHeaderViewItem) ?: return@setOnSafeClickListener

            onViewItemClick(viewItem)
        }

        return viewHolder
    }

    override fun onViewItemClick(item: SOSHeaderViewItem) {

        super.onViewItemClick(item)

        AppEventBus.post(AppEvent.SOSItemClicked(SettingItem.ID_EMERGENCY_CALL_TOGGLE))
    }

    override fun onBindViewHolder(
        binding: ItemSosHeaderBinding,
        viewType: Int,
        position: Int,
        item: SOSHeaderViewItem,
        payloads: List<String>
    ) {

        if (payloads.isEmpty() || payloads.contains("title")) {

            binding.tvTitle.setText(item.title)
        }

        if (payloads.isEmpty() || payloads.contains("desc")) {

            binding.tvDesc.setText(item.desc)
        }

        if (payloads.isEmpty() || payloads.contains("icon")) {

            binding.ivIcon.setImage(item.icon)
        }

        if (payloads.isEmpty() || payloads.contains("iconBackground")) {

            binding.ivIconBg.setBackground(item.iconBackground)
        }

        if (payloads.isEmpty() || payloads.contains("isEnabled")) {

            binding.swSos.isChecked = item.isEnabled
        }
    }
}
