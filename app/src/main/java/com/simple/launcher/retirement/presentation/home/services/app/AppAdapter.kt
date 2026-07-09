package com.simple.launcher.retirement.presentation.home.services.app

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.databinding.ItemNodeBinding
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.getItem
import com.simple.ui.precompute.DrawSpec
import com.simple.launcher.retirement.utils.view.setOnSafeWithPerformHapticFeedbackClickListener

data class AppHomeItem(
    val entity: AppEntity,
    val spec: DrawSpec
) : HomeItem {

    override fun areItemsTheSame(): List<Any> = listOf(
        entity.packageName
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        spec to "spec"
    )
}

@Adapter
class AppAdapter : ViewItemAdapter<AppHomeItem, ItemNodeBinding>() {

    override val viewItemClass: Class<AppHomeItem> by lazy {

        AppHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemNodeBinding {

        return ItemNodeBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemNodeBinding> {

        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeWithPerformHapticFeedbackClickListener {

            val item = viewHolder.getItem<AppHomeItem>() ?: return@setOnSafeWithPerformHapticFeedbackClickListener
            sendDeeplink(DeepLinks.APP, mapOf("entity" to item.entity))
        }

        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemNodeBinding, viewType: Int, position: Int, item: AppHomeItem, payloads: List<String>) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("spec")) {

            binding.nodeView.spec = item.spec
        }
    }
}
