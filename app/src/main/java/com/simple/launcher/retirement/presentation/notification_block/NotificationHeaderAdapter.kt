package com.simple.launcher.retirement.presentation.notification_block

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSosHeaderBinding
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

/**
 * Header card master-toggle của màn Notification Block — tái dùng layout `item_sos_header.xml`
 * để đồng bộ style với Advanced SOS. Không tái dùng SOSHeaderAdapter vì adapter đó phát
 * `SOSItemClicked(ID_EMERGENCY_CALL_TOGGLE)` cứng nhắc; ở đây cần event riêng.
 */
data class NotificationHeaderViewItem(
    val title: BigText,
    val desc: BigText,
    val icon: BigImage,
    val iconBackground: Background = Background(),
    val isEnabled: Boolean
) : ViewItem, SpanSizeLookupViewItem {

    override fun getSpanSize(): Int = 2

    override fun areItemsTheSame(): List<Any> = listOf("NotificationBlockHeader")

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",
        desc to "desc",
        icon to "icon",
        iconBackground to "iconBackground",
        isEnabled to "isEnabled"
    )
}

@Adapter
class NotificationHeaderAdapter : ViewItemAdapter<NotificationHeaderViewItem, ItemSosHeaderBinding>() {

    override val viewItemClass: Class<NotificationHeaderViewItem> =
        NotificationHeaderViewItem::class.java

    override fun createViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSosHeaderBinding {

        return ItemSosHeaderBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseBindingViewHolder<ItemSosHeaderBinding> {

        val viewHolder = super.createViewHolder(parent, viewType)

        viewHolder.binding.vClickToggle.setOnSafeClickListener {

            AppEventBus.post(AppEvent.NotificationBlockHeaderClicked)
        }
        return viewHolder
    }

    override fun onBindViewHolder(
        binding: ItemSosHeaderBinding,
        viewType: Int,
        position: Int,
        item: NotificationHeaderViewItem,
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
