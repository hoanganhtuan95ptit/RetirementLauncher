package com.simple.launcher.retirement.presentation.notification_block

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ItemNotificationAppCardBinding
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.exts.getItem
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class NotificationBlockAppItem(
    val label: BigText,
    val icon: BigImage,
    val isSelected: Boolean,
    val entity: AppEntity
) : ViewItem, SpanSizeLookupViewItem {

    override fun getSpanSize(): Int = 2

    override fun areItemsTheSame(): List<Any> = listOf(TYPE_TAG, entity.packageName)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label",
        icon to "icon",
        isSelected to "isSelected"
    )

    companion object {

        private const val TYPE_TAG = "notification_block_app"
    }
}

@Adapter
class NotificationBlockAppAdapter : ViewItemAdapter<NotificationBlockAppItem, ItemNotificationAppCardBinding>() {

    override val viewItemClass: Class<NotificationBlockAppItem> by lazy {

        NotificationBlockAppItem::class.java
    }

    override fun createViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemNotificationAppCardBinding {

        return ItemNotificationAppCardBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseBindingViewHolder<ItemNotificationAppCardBinding> {

        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeClickListener {

            val item = viewHolder.getItem<NotificationBlockAppItem>() ?: return@setOnSafeClickListener
            AppEventBus.post(AppEvent.NotificationBlockAppToggled(item.entity))
        }
        return viewHolder
    }

    override fun onBindViewHolder(
        binding: ItemNotificationAppCardBinding,
        viewType: Int,
        position: Int,
        item: NotificationBlockAppItem,
        payloads: List<String>
    ) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("label")) {

            binding.tvAppLabel.setText(item.label)
        }

        if (payloads.isEmpty() || payloads.contains("icon")) {

            binding.ivAppIcon.setImage(item.icon)
        }

        if (payloads.isEmpty() || payloads.contains("isSelected")) {

            val checkIcon = if (item.isSelected) {

                R.drawable.ic_radio_selected
            } else {

                R.drawable.ic_radio_unselected
            }
            binding.ivCheck.setImageResource(checkIcon)
        }
    }
}
