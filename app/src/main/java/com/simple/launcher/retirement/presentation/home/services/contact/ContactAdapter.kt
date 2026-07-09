package com.simple.launcher.retirement.presentation.home.services.contact

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.databinding.ItemContactBinding
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.getItem
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeWithPerformHapticFeedbackClickListener

data class ContactHomeItem(
    val entity: ContactEntity,
    val name: BigText,
    val photo: BigImage,
    val background: Background,

    val tapToCallLabel: BigText,
    val tapToCallBackground: Background,
) : HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS / 2 // half width

    override fun areItemsTheSame(): List<Any> = listOf(
        entity.id
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        name to "name",
        photo to "photo",
        background to "background",

        tapToCallLabel to "tapToCallLabel",
        tapToCallBackground to "tapToCallBackground"
    )
}

@Adapter
class ContactAdapter : ViewItemAdapter<ContactHomeItem, ItemContactBinding>() {

    override val viewItemClass: Class<ContactHomeItem> by lazy {

        ContactHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemContactBinding {

        return ItemContactBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemContactBinding> {

        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeWithPerformHapticFeedbackClickListener {

            val item = viewHolder.getItem<ContactHomeItem>() ?: return@setOnSafeWithPerformHapticFeedbackClickListener
            sendDeeplink(DeepLinks.CALL, mapOf("entity" to item.entity))
        }

        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemContactBinding, viewType: Int, position: Int, item: ContactHomeItem, payloads: List<String>) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("name")) {

            binding.tvName.setText(item.name)
        }

        if (payloads.isEmpty() || payloads.contains("photo")) {

            binding.ivPhoto.setImage(item.photo)
        }

        if (payloads.isEmpty() || payloads.contains("background")) {

            binding.root.setBackground(item.background)
        }

        if (payloads.isEmpty() || payloads.contains("tapToCallLabel")) {

            binding.tvTapToCall.setText(item.tapToCallLabel)
        }

        if (payloads.isEmpty() || payloads.contains("tapToCallBackground")) {

            binding.tvTapToCall.setBackground(item.tapToCallBackground)
        }
    }
}
