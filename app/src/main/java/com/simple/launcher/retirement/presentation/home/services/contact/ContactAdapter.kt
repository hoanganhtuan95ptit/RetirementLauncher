package com.simple.launcher.retirement.presentation.home.services.contact

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.databinding.ItemNodeBinding
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.getItem
import com.simple.ui.precompute.DrawSpec
import com.simple.launcher.retirement.utils.view.setOnSafeWithPerformHapticFeedbackClickListener

data class ContactHomeItem(
    val entity: ContactEntity,
    val spec: DrawSpec
) : HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS / 2 // half width

    override fun areItemsTheSame(): List<Any> = listOf(
        entity.id
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        spec to "spec"
    )
}

@Adapter
class ContactAdapter : ViewItemAdapter<ContactHomeItem, ItemNodeBinding>() {

    override val viewItemClass: Class<ContactHomeItem> by lazy {

        ContactHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemNodeBinding {

        return ItemNodeBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemNodeBinding> {

        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeWithPerformHapticFeedbackClickListener {

            val item = viewHolder.getItem<ContactHomeItem>() ?: return@setOnSafeWithPerformHapticFeedbackClickListener
            sendDeeplink(DeepLinks.CALL, mapOf("entity" to item.entity))
        }

        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemNodeBinding, viewType: Int, position: Int, item: ContactHomeItem, payloads: List<String>) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("spec")) {

            binding.nodeView.spec = item.spec
        }
    }
}
